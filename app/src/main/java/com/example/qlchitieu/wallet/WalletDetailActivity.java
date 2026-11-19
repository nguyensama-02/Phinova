package com.example.qlchitieu.wallet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlchitieu.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.text.NumberFormat;
import java.util.Locale;

public class WalletDetailActivity extends AppCompatActivity {

    TextView tvQuayLaiChiTietVi;
    TextView tvSoDuChiTietVi, tvTenViTieuDe;
    TextView tvTenViChiTiet, tvLoaiViChiTiet, tvTrangThaiVi;

    MaterialButton btnSuaVi, btnDieuChinhSoDu;
    MaterialButton btnLichSuDieuChinh, btnQuanLyVi;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    String maVi;
    String trangThaiVi = "HOAT_DONG";

    boolean dangXuLy = false;
    boolean daTaiVi = false;

    NumberFormat dinhDangTien =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_detail);

        tvQuayLaiChiTietVi = findViewById(R.id.tvQuayLaiChiTietVi);
        tvSoDuChiTietVi = findViewById(R.id.tvSoDuChiTietVi);
        tvTenViTieuDe = findViewById(R.id.tvTenViTieuDe);
        tvTenViChiTiet = findViewById(R.id.tvTenViChiTiet);
        tvLoaiViChiTiet = findViewById(R.id.tvLoaiViChiTiet);
        tvTrangThaiVi = findViewById(R.id.tvTrangThaiVi);

        btnSuaVi = findViewById(R.id.btnSuaVi);
        btnDieuChinhSoDu = findViewById(R.id.btnDieuChinhSoDu);
        btnLichSuDieuChinh = findViewById(R.id.btnLichSuDieuChinh);
        btnQuanLyVi = findViewById(R.id.btnQuanLyVi);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        maVi = getIntent().getStringExtra("maVi");

        tvQuayLaiChiTietVi.setOnClickListener(v -> finish());

        btnSuaVi.setOnClickListener(v -> {
            if (!"HOAT_DONG".equals(trangThaiVi)) {
                return;
            }

            Intent intent = new Intent(
                    WalletDetailActivity.this,
                    EditWalletActivity.class
            );
            intent.putExtra("maVi", maVi);
            startActivity(intent);
        });

        btnDieuChinhSoDu.setOnClickListener(v -> {
            if (!"HOAT_DONG".equals(trangThaiVi)) {
                return;
            }

            Intent intent = new Intent(
                    WalletDetailActivity.this,
                    AdjustBalanceActivity.class
            );
            intent.putExtra("maVi", maVi);
            startActivity(intent);
        });

        btnLichSuDieuChinh.setOnClickListener(v -> {
            Intent intent = new Intent(
                    WalletDetailActivity.this,
                    BalanceHistoryActivity.class
            );
            intent.putExtra("maVi", maVi);
            startActivity(intent);
        });

        btnQuanLyVi.setOnClickListener(v -> hienThiQuanLyVi());

        btnSuaVi.setEnabled(false);
        btnDieuChinhSoDu.setEnabled(false);
        btnQuanLyVi.setEnabled(false);

        if (maVi == null || maVi.trim().isEmpty()) {
            thongBao("Không tìm thấy mã ví");
            finish();
            return;
        }

        taiChiTietVi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (maVi != null && !maVi.trim().isEmpty()) {
            taiChiTietVi();
        }
    }

    private DocumentReference layViReference() {
        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            return null;
        }

        return firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("wallets")
                .document(maVi);
    }

    private void taiChiTietVi() {
        DocumentReference viRef = layViReference();

        if (viRef == null) {
            thongBao("Vui lòng đăng nhập lại");
            finish();
            return;
        }

        viRef.get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        thongBao("Ví không tồn tại");
                        finish();
                        return;
                    }

                    String tenVi = document.getString("tenVi");
                    String loaiVi = document.getString("loaiVi");
                    Long soDu = document.getLong("soDu");
                    String trangThai = document.getString("trangThai");

                    if (tenVi == null) {
                        tenVi = "Ví chưa đặt tên";
                    }

                    if (loaiVi == null) {
                        loaiVi = "Khác";
                    }

                    if (soDu == null) {
                        soDu = 0L;
                    }

                    tvSoDuChiTietVi.setText(
                            dinhDangTien.format(soDu) + " ₫"
                    );

                    tvTenViTieuDe.setText(tenVi);
                    tvTenViChiTiet.setText(tenVi);
                    tvLoaiViChiTiet.setText(loaiVi);

                    if ("LUU_TRU".equals(trangThai)) {
                        trangThaiVi = "LUU_TRU";

                        tvTrangThaiVi.setText("Đã lưu trữ");
                        tvTrangThaiVi.setTextColor(
                                Color.parseColor("#64748B")
                        );

                        btnSuaVi.setEnabled(false);
                        btnDieuChinhSoDu.setEnabled(false);

                    } else {
                        trangThaiVi = "HOAT_DONG";

                        tvTrangThaiVi.setText("Đang sử dụng");
                        tvTrangThaiVi.setTextColor(
                                Color.parseColor("#059669")
                        );

                        btnSuaVi.setEnabled(true);
                        btnDieuChinhSoDu.setEnabled(true);
                    }

                    daTaiVi = true;
                    btnQuanLyVi.setEnabled(!dangXuLy);
                })
                .addOnFailureListener(e -> {
                    thongBao("Không thể tải chi tiết ví: "
                            + e.getMessage());
                });
    }

    private void hienThiQuanLyVi() {
        if (dangXuLy || !daTaiVi) {
            return;
        }

        boolean daLuuTru = "LUU_TRU".equals(trangThaiVi);

        String[] luaChon = {
                daLuuTru ? "Khôi phục ví" : "Lưu trữ ví",
                "Xóa ví vĩnh viễn"
        };

        new AlertDialog.Builder(this)
                .setTitle("Quản lý ví")
                .setItems(luaChon, (dialog, which) -> {
                    if (which == 0) {
                        xacNhanDoiTrangThaiVi();
                    } else {
                        kiemTraVaXoaVi();
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void xacNhanDoiTrangThaiVi() {
        boolean daLuuTru = "LUU_TRU".equals(trangThaiVi);

        String tieuDe = daLuuTru
                ? "Khôi phục ví"
                : "Lưu trữ ví";

        String noiDung = daLuuTru
                ? "Bạn muốn tiếp tục sử dụng ví này?"
                : "Bạn muốn ngừng sử dụng ví này?\n\n"
                  + "Ví và lịch sử vẫn được giữ lại.\n"
                  + "Chỉ có thể lưu trữ ví khi số dư bằng 0 ₫.";

        new AlertDialog.Builder(this)
                .setTitle(tieuDe)
                .setMessage(noiDung)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        daLuuTru ? "Khôi phục" : "Lưu trữ",
                        (dialog, which) -> doiTrangThaiVi(daLuuTru)
                )
                .show();
    }

    private void doiTrangThaiVi(boolean daLuuTru) {
        DocumentReference viRef = layViReference();

        if (viRef == null || dangXuLy) {
            return;
        }

        batDauXuLy();

        firestore.runTransaction(transaction -> {
            DocumentSnapshot document = transaction.get(viRef);

            if (!document.exists()) {
                throw new IllegalStateException("Ví không tồn tại");
            }

            String trangThai = document.getString("trangThai");

            boolean dangLuuTru = "LUU_TRU".equals(trangThai);

            if (dangLuuTru != daLuuTru) {
                throw new IllegalStateException(
                        "Trạng thái ví đã thay đổi, hãy tải lại"
                );
            }

            if (daLuuTru) {
                transaction.update(
                        viRef,
                        "trangThai", "HOAT_DONG",
                        "ngayLuuTru", null
                );
            } else {
                Long soDu = document.getLong("soDu");

                if (soDu == null || soDu != 0) {
                    throw new IllegalStateException(
                            "Cần đưa số dư về 0 ₫ trước khi lưu trữ"
                    );
                }

                transaction.update(
                        viRef,
                        "trangThai", "LUU_TRU",
                        "ngayLuuTru", FieldValue.serverTimestamp()
                );
            }

            return null;
        }).addOnSuccessListener(unused -> {
            thongBao(daLuuTru
                    ? "Khôi phục ví thành công"
                    : "Lưu trữ ví thành công");

            ketThucXuLy();
            taiChiTietVi();

        }).addOnFailureListener(e -> {
            thongBao("Không thể thay đổi trạng thái: "
                    + e.getMessage());

            ketThucXuLy();
            taiChiTietVi();
        });
    }

    private void kiemTraVaXoaVi() {
        DocumentReference viRef = layViReference();

        if (viRef == null || dangXuLy) {
            return;
        }

        batDauXuLy();

        viRef.get(Source.SERVER)
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        thongBao("Ví không tồn tại");
                        ketThucXuLy();
                        finish();
                        return;
                    }

                    Long soDu = document.getLong("soDu");

                    if (soDu == null || soDu != 0) {
                        new AlertDialog.Builder(this)
                                .setTitle("Không thể xóa ví")
                                .setMessage(
                                        "Chỉ có thể xóa ví khi "
                                                + "số dư bằng 0 ₫."
                                )
                                .setPositiveButton("Đã hiểu", null)
                                .show();

                        ketThucXuLy();
                        return;
                    }

                    kiemTraLichSuVi(viRef);
                })
                .addOnFailureListener(e -> {
                    thongBao("Không thể kiểm tra ví: "
                            + e.getMessage());
                    ketThucXuLy();
                });
    }

    private void kiemTraLichSuVi(DocumentReference viRef) {
        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            thongBao("Vui lòng đăng nhập lại");
            ketThucXuLy();
            return;
        }

        String uid = nguoiDung.getUid();

        CollectionReference giaoDichRef = firestore
                .collection("users")
                .document(uid)
                .collection("transactions");

        CollectionReference dieuChinhRef = firestore
                .collection("users")
                .document(uid)
                .collection("balance_adjustments");

        Task<QuerySnapshot> giaoDichNguon = giaoDichRef
                .whereEqualTo("maViNguon", maVi)
                .limit(1)
                .get(Source.SERVER);

        Task<QuerySnapshot> giaoDichDich = giaoDichRef
                .whereEqualTo("maViDich", maVi)
                .limit(1)
                .get(Source.SERVER);

        Task<QuerySnapshot> lichSuDieuChinh = dieuChinhRef
                .whereEqualTo("maVi", maVi)
                .limit(1)
                .get(Source.SERVER);

        Tasks.whenAllSuccess(
                giaoDichNguon,
                giaoDichDich,
                lichSuDieuChinh
        ).addOnSuccessListener(ketQua -> {
            boolean coGiaoDichNguon =
                    !giaoDichNguon.getResult().isEmpty();

            boolean coGiaoDichDich =
                    !giaoDichDich.getResult().isEmpty();

            boolean coLichSuDieuChinh =
                    !lichSuDieuChinh.getResult().isEmpty();

            if (coGiaoDichNguon
                    || coGiaoDichDich
                    || coLichSuDieuChinh) {

                new AlertDialog.Builder(this)
                        .setTitle("Không thể xóa ví")
                        .setMessage(
                                "Ví đã có giao dịch hoặc lịch sử "
                                        + "điều chỉnh số dư.\n\n"
                                        + "Hãy sử dụng chức năng Lưu trữ ví "
                                        + "để giữ lại dữ liệu tài chính."
                        )
                        .setPositiveButton("Đã hiểu", null)
                        .show();

                ketThucXuLy();
                return;
            }

            hienThiXacNhanXoa(viRef);

        }).addOnFailureListener(e -> {
            thongBao("Không thể kiểm tra lịch sử: "
                    + e.getMessage());
            ketThucXuLy();
        });
    }

    private void hienThiXacNhanXoa(DocumentReference viRef) {
        String tenVi = tvTenViChiTiet.getText().toString();

        new AlertDialog.Builder(this)
                .setTitle("Xóa ví vĩnh viễn")
                .setMessage(
                        "Bạn có chắc muốn xóa ví \""
                                + tenVi + "\"?\n\n"
                                + "Hành động này không thể hoàn tác."
                )
                .setNegativeButton("Hủy",
                        (dialog, which) -> ketThucXuLy())
                .setPositiveButton("Xóa ví",
                        (dialog, which) -> thucHienXoaVi(viRef))
                .setOnCancelListener(dialog -> ketThucXuLy())
                .show();
    }

    private void thucHienXoaVi(DocumentReference viRef) {
        viRef.delete()
                .addOnSuccessListener(unused -> {
                    thongBao("Xóa ví thành công");
                    ketThucXuLy();
                    finish();
                })
                .addOnFailureListener(e -> {
                    thongBao("Xóa ví thất bại: "
                            + e.getMessage());
                    ketThucXuLy();
                });
    }

    private void batDauXuLy() {
        dangXuLy = true;
        btnQuanLyVi.setEnabled(false);
    }

    private void ketThucXuLy() {
        dangXuLy = false;
        btnQuanLyVi.setEnabled(true);
    }

    private void thongBao(String noiDung) {
        Toast.makeText(this,
                noiDung,
                Toast.LENGTH_LONG).show();
    }
}
