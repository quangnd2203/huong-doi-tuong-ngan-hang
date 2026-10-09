package vn.edu.pxu.bank.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import vn.edu.pxu.bank.model.account.BankAccount;
import vn.edu.pxu.bank.model.account.CheckingAccount;
import vn.edu.pxu.bank.model.account.LoanAccount;
import vn.edu.pxu.bank.model.account.SavingsAccount;
import vn.edu.pxu.bank.model.transaction.Transaction;
import vn.edu.pxu.bank.model.user.Customer;
import vn.edu.pxu.bank.model.user.Employee;
import vn.edu.pxu.bank.model.user.User;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Một nguồn dữ liệu dùng chung. Tài khoản chỉ nằm trong Customer.accounts;
 * giao dịch lưu một lần ở danh sách transactions.
 *
 * Phải gọi load() thành công trước khi ghi để tránh ghi đè file hỏng.
 */
public final class JsonRepository {
    private static final Path DEFAULT_FILE = Path.of("data", "bank-data.json");
    private static final Gson GSON = createGson();
    private static final JsonRepository INSTANCE = new JsonRepository();

    private Path file = DEFAULT_FILE;
    private boolean loaded;
    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    private JsonRepository() {
    }

    public static JsonRepository getInstance() {
        return INSTANCE;
    }

    /** Danh sách dữ liệu dùng chung cho các service; sau khi sửa phải gọi save(). */
    public List<User> getUsers() {
        requireLoaded();
        return users;
    }

    /** Danh sách giao dịch duy nhất, không nhúng vào từng tài khoản. */
    public List<Transaction> getTransactions() {
        requireLoaded();
        return transactions;
    }

    public synchronized void load() {
        load(DEFAULT_FILE);
    }

    /** Đọc lại đúng file đang sử dụng, ví dụ khi cần khôi phục sau lỗi lưu. */
    public synchronized void reload() {
        load(file);
    }

    // Cho phép kiểm thử với file tạm; ứng dụng luôn gọi load() không tham số.
    synchronized void load(Path path) {
        file = Objects.requireNonNull(path, "Đường dẫn không được null").toAbsolutePath();
        loaded = false;

        // notExists chỉ true khi xác nhận file thực sự không tồn tại.
        if (Files.notExists(file)) {
            users = new ArrayList<>();
            transactions = new ArrayList<>();
            loaded = true;
            return;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement json = JsonParser.parseReader(reader);
            if (!json.isJsonObject()) {
                throw new JsonParseException("Gốc JSON phải là object");
            }

            JsonObject root = json.getAsJsonObject();
            if (!root.keySet().equals(Set.of("users", "transactions"))) {
                throw new JsonParseException("JSON chỉ được có hai mảng users và transactions");
            }
            JsonArray userJson = requiredArray(root, "users");
            JsonArray transactionJson = requiredArray(root, "transactions");

            List<User> newUsers = new ArrayList<>();
            List<Transaction> newTransactions = new ArrayList<>();
            for (JsonElement item : userJson) {
                newUsers.add(rebuildUser(GSON.fromJson(item, User.class)));
            }
            for (JsonElement item : transactionJson) {
                newTransactions.add(rebuildTransaction(GSON.fromJson(item, Transaction.class)));
            }
            checkIntegrity(newUsers, newTransactions);

            // Chỉ công bố dữ liệu nếu đã nạp và kiểm tra toàn bộ thành công.
            users = newUsers;
            transactions = newTransactions;
            loaded = true;
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException(
                    "Không đọc được file dữ liệu ngân hàng " + file
                            + ". File được giữ nguyên; ứng dụng không được ghi đè.", ex);
        }
    }

    /** Ghi cả người dùng/tài khoản và giao dịch trong một lần thay file. */
    public synchronized void save() {
        requireLoaded();
        checkIntegrity(users, transactions);

        JsonObject root = new JsonObject();
        JsonArray userJson = new JsonArray();
        for (User user : users) {
            userJson.add(GSON.toJsonTree(user, User.class));
        }
        JsonArray transactionJson = new JsonArray();
        for (Transaction transaction : transactions) {
            transactionJson.add(GSON.toJsonTree(transaction, Transaction.class));
        }
        root.add("users", userJson);
        root.add("transactions", transactionJson);

        Path temporary = null;
        try {
            Path directory = file.getParent();
            Files.createDirectories(directory);
            temporary = Files.createTempFile(directory, "bank-data-", ".tmp");
            Files.writeString(temporary, GSON.toJson(root), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể lưu dữ liệu ngân hàng vào " + file, ex);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Không che mất lỗi ghi dữ liệu chính.
                }
            }
        }
    }

    private void requireLoaded() {
        if (!loaded) {
            throw new IllegalStateException("Phải load() thành công trước khi sử dụng repository");
        }
    }

    private static JsonArray requiredArray(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonArray()) {
            throw new JsonParseException("Thiếu mảng dữ liệu " + key);
        }
        return root.getAsJsonArray(key);
    }

    private static User rebuildUser(User user) {
        if (user instanceof Employee employee) {
            return new Employee(employee.getUserId(), employee.getUsername(),
                    employee.getPasswordHash(), employee.getStatus(),
                    employee.isMustChangePassword());
        }
        if (user instanceof Customer customer) {
            Customer restored = new Customer(customer.getUserId(), customer.getUsername(),
                    customer.getPasswordHash(), customer.getStatus(), customer.isMustChangePassword(),
                    customer.getCustomerId(), customer.getFullName(), customer.getPhone(),
                    customer.getEmail(), customer.getAddress());
            for (BankAccount account : customer.getAccounts()) {
                restored.addAccount(rebuildAccount(account));
            }
            return restored;
        }
        throw new JsonParseException("Loại người dùng không hỗ trợ");
    }

    private static BankAccount rebuildAccount(BankAccount account) {
        Objects.requireNonNull(account, "Tài khoản không được null");
        if (account instanceof CheckingAccount checking) {
            return new CheckingAccount(checking.getAccountNumber(), checking.getCustomerId(),
                    checking.getBalance(), checking.getStatus());
        }
        if (account instanceof SavingsAccount savings) {
            return new SavingsAccount(savings.getAccountNumber(), savings.getCustomerId(),
                    savings.getBalance(), savings.getTermMonths(), savings.getAnnualRate(),
                    savings.getEarlyWithdrawalRate(), savings.getStartDate(), savings.getEndDate(),
                    savings.getStatus());
        }
        if (account instanceof LoanAccount loan) {
            return new LoanAccount(loan.getAccountNumber(), loan.getCustomerId(),
                    loan.getBalance(), loan.getAnnualRate(), loan.getStartDate(),
                    loan.getEndDate(), loan.getStatus());
        }
        throw new JsonParseException("Loại tài khoản không hỗ trợ");
    }

    private static Transaction rebuildTransaction(Transaction tx) {
        Objects.requireNonNull(tx, "Giao dịch không được null");
        return new Transaction(tx.getTransactionId(), tx.getAmount(),
                tx.getFromAccountNumber(), tx.getToAccountNumber(),
                tx.getType(), tx.getOccurredAt(), tx.getNote());
    }

    private static void checkIntegrity(List<User> users, List<Transaction> transactions) {
        Set<String> userIds = new HashSet<>();
        Set<String> usernames = new HashSet<>();
        Set<String> customerIds = new HashSet<>();
        Set<String> accountNumbers = new HashSet<>();
        Set<String> transactionIds = new HashSet<>();

        for (User user : users) {
            Objects.requireNonNull(user, "Danh sách có người dùng null");
            if (!userIds.add(user.getUserId()) || !usernames.add(user.getUsername())) {
                throw new IllegalStateException("Trùng mã người dùng hoặc tên đăng nhập");
            }
            if (user instanceof Customer customer) {
                if (!customerIds.add(customer.getCustomerId())) {
                    throw new IllegalStateException("Trùng mã khách hàng");
                }
                for (BankAccount account : customer.getAccounts()) {
                    if (!customer.getCustomerId().equals(account.getCustomerId())
                            || !accountNumbers.add(account.getAccountNumber())) {
                        throw new IllegalStateException("Tài khoản trùng hoặc sai chủ sở hữu");
                    }
                }
            }
        }
        for (Transaction transaction : transactions) {
            Objects.requireNonNull(transaction, "Danh sách có giao dịch null");
            if (!transactionIds.add(transaction.getTransactionId())) {
                throw new IllegalStateException("Trùng mã giao dịch");
            }
        }
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    @Override
                    public void write(JsonWriter out, LocalDate value) throws IOException {
                        out.value(value == null ? null : value.toString());
                    }

                    @Override
                    public LocalDate read(JsonReader in) throws IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        return LocalDate.parse(in.nextString());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter out, LocalDateTime value) throws IOException {
                        out.value(value == null ? null : value.toString());
                    }

                    @Override
                    public LocalDateTime read(JsonReader in) throws IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        return LocalDateTime.parse(in.nextString());
                    }
                })
                .registerTypeAdapter(User.class, new JsonSerializer<User>() {
                    @Override
                    public JsonElement serialize(User user, java.lang.reflect.Type type,
                                                 JsonSerializationContext context) {
                        JsonObject result = context.serialize(user, user.getClass()).getAsJsonObject();
                        result.addProperty("kind", user instanceof Customer ? "CUSTOMER" : "EMPLOYEE");
                        return result;
                    }
                })
                .registerTypeAdapter(User.class, new JsonDeserializer<User>() {
                    @Override
                    public User deserialize(JsonElement element, java.lang.reflect.Type type,
                                            JsonDeserializationContext context) throws JsonParseException {
                        JsonObject object = element.getAsJsonObject();
                        String kind = object.get("kind").getAsString();
                        return switch (kind) {
                            case "CUSTOMER" -> context.deserialize(object, Customer.class);
                            case "EMPLOYEE" -> context.deserialize(object, Employee.class);
                            default -> throw new JsonParseException("Loại người dùng không hợp lệ: " + kind);
                        };
                    }
                })
                .registerTypeAdapter(BankAccount.class, new JsonSerializer<BankAccount>() {
                    @Override
                    public JsonElement serialize(BankAccount account, java.lang.reflect.Type type,
                                                 JsonSerializationContext context) {
                        JsonObject result = context.serialize(account, account.getClass()).getAsJsonObject();
                        result.addProperty("kind", switch (account.getAccountType()) {
                            case CHECKING -> "CHECKING";
                            case SAVINGS -> "SAVINGS";
                            case LOAN -> "LOAN";
                        });
                        return result;
                    }
                })
                .registerTypeAdapter(BankAccount.class, new JsonDeserializer<BankAccount>() {
                    @Override
                    public BankAccount deserialize(JsonElement element, java.lang.reflect.Type type,
                                                    JsonDeserializationContext context) throws JsonParseException {
                        JsonObject object = element.getAsJsonObject();
                        String kind = object.get("kind").getAsString();
                        return switch (kind) {
                            case "CHECKING" -> context.deserialize(object, CheckingAccount.class);
                            case "SAVINGS" -> context.deserialize(object, SavingsAccount.class);
                            case "LOAN" -> context.deserialize(object, LoanAccount.class);
                            default -> throw new JsonParseException("Loại tài khoản không hợp lệ: " + kind);
                        };
                    }
                })
                .create();
    }
}
