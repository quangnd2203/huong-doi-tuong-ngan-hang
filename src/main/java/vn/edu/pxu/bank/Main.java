package vn.edu.pxu.bank;

import vn.edu.pxu.bank.di.DI;
import vn.edu.pxu.bank.ui.BankWindow;

/** Điểm vào ứng dụng: DI khởi tạo trước, UI chỉ nhận các Service qua DI. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        BankWindow.launch(DI.getInstance());
    }
}
