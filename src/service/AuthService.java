package service;

import dao.TaiKhoanDAO;
import model.TaiKhoan;
import utils.PasswordUtils;

import java.util.Optional;

// Dịch vụ xác thực tài khoản
public class AuthService {

    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    private static TaiKhoan currentUser = null;

    // Đăng nhập
    public TaiKhoan login(String username, String password) throws Exception {

        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Mật khẩu không được để trống.");

        Optional<TaiKhoan> opt = taiKhoanDAO.findByUsername(username.trim());

        if (opt.isEmpty())
            throw new Exception("Tên đăng nhập không tồn tại.");

        TaiKhoan tk = opt.get();

        if (!"Active".equals(tk.getTrangThai()))
            throw new Exception("Tài khoản đã bị vô hiệu hoá. Liên hệ Admin.");

        if (!PasswordUtils.verify(password, tk.getPasswordHash()))
            throw new Exception("Mật khẩu không đúng.");

        currentUser = tk;

        System.out.println("[AUTH] Login success: " + tk.getUsername());

        return tk;
    }

    // Đăng ký
    public TaiKhoan register(String username, String password, String hoTen) throws Exception {

        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Mật khẩu không được để trống.");

        if (hoTen == null || hoTen.isBlank())
            throw new IllegalArgumentException("Họ tên không được để trống.");

        String trimmedUsername = username.trim();

        if (taiKhoanDAO.isUsernameExists(trimmedUsername))
            throw new Exception("Tên đăng nhập đã tồn tại.");

        TaiKhoan newAccount = new TaiKhoan();
        newAccount.setUsername(trimmedUsername);
        newAccount.setPasswordHash(PasswordUtils.hash(password));
        newAccount.setHoTen(hoTen.trim());
        newAccount.setRoleID(2);
        newAccount.setTrangThai("Active");

        boolean ok = taiKhoanDAO.insert(newAccount);

        if (!ok)
            throw new Exception("Đăng ký thất bại.");

        return taiKhoanDAO.findByUsername(trimmedUsername)
                .orElseThrow(() -> new Exception("Không tìm thấy tài khoản sau khi tạo."));
    }

    public void logout() {
        currentUser = null;
    }

    public static TaiKhoan getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}