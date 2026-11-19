package com.example.qlchitieu.transaction;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.qlchitieu.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Source;
import java.util.HashMap;
import java.util.Map;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionDetailActivity extends AppCompatActivity {

    TextView tvQuayLaiChiTietGiaoDich;
    TextView tvLoaiGiaoDichChiTiet, tvSoTienChiTietGiaoDich;
    TextView tvDanhMucChiTietGiaoDich, tvLoaiChiTiet;
    TextView tvViNguonChiTiet, tvViDichChiTiet;
    TextView tvNgayChiTietGiaoDich, tvGhiChuChiTietGiaoDich;

    MaterialButton btnSuaGiaoDich, btnXoaGiaoDich;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    String maGiaoDich;

    NumberFormat dinhDangTien =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    SimpleDateFormat dinhDangNgay =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        tvQuayLaiChiTietGiaoDich =
                findViewById(R.id.tvQuayLaiChiTietGiaoDich);

        tvLoaiGiaoDichChiTiet =
                findViewById(R.id.tvLoaiGiaoDichChiTiet);

        tvSoTienChiTietGiaoDich =
                findViewById(R.id.tvSoTienChiTietGiaoDich);

        tvDanhMucChiTietGiaoDich =
                findViewById(R.id.tvDanhMucChiTietGiaoDich);

        tvLoaiChiTiet = findViewById(R.id.tvLoaiChiTiet);
        tvViNguonChiTiet = findViewById(R.id.tvViNguonChiTiet);
        tvViDichChiTiet = findViewById(R.id.tvViDichChiTiet);

        tvNgayChiTietGiaoDich =
                findViewById(R.id.tvNgayChiTietGiaoDich);

        tvGhiChuChiTietGiaoDich =
                findViewById(R.id.tvGhiChuChiTietGiaoDich);

        btnSuaGiaoDich = findViewById(R.id.btnSuaGiaoDich);
        btnXoaGiaoDich = findViewById(R.id.btnXoaGiaoDich);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        maGiaoDich = getIntent().getStringExtra("maGiaoDich");

        btnXoaGiaoDich.setOnClickListener(v -> {
            xacNhanXoaGiaoDich();
        });

        btnSuaGiaoDich.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TransactionDetailActivity.this,
                    EditTransactionActivity.class
            );

            intent.putExtra("maGiaoDich", maGiaoDich);

            startActivity(intent);
        });

        tvQuayLaiChiTietGiaoDich.setOnClickListener(v -> finish());

        btnSuaGiaoDich.setEnabled(false);
        btnXoaGiaoDich.setEnabled(false);

        if (maGiaoDich == null || maGiaoDich.trim().isEmpty()) {
            Toast.makeText(this,
                    "Không tìm thấy mã giao dịch",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        taiChiTietGiaoDich();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (maGiaoDich != null && !maGiaoDich.trim().isEmpty()) {
            taiChiTietGiaoDich();
        }
    }

    private void taiChiTietGiaoDich() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("transactions")
                .document(maGiaoDich)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        Toast.makeText(this,
                                "Giao dịch không tồn tại",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    hienThiChiTiet(document);

                    String loai = document.getString("loaiGiaoDich");

                    boolean coTheXoa =
                            "Thu nhập".equals(loai)
                                    || "Chi tiêu".equals(loai)
                                    || "Chuyển tiền".equals(loai);

                    btnXoaGiaoDich.setEnabled(coTheXoa);

                    boolean duocChinhSua =
                            "Thu nhập".equals(loai)
                                    || "Chi tiêu".equals(loai)
                                    || "Chuyển tiền".equals(loai);

                    btnSuaGiaoDich.setEnabled(duocChinhSua);

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Không thể tải giao dịch: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void hienThiChiTiet(DocumentSnapshot document) {

        String loai = document.getString("loaiGiaoDich");
        String danhMuc = document.getString("danhMuc");
        String ghiChu = document.getString("ghiChu");

        String maViNguon = document.getString("maViNguon");
        String maViDich = document.getString("maViDich");

        Long soTien = document.getLong("soTien");
        Date ngayGiaoDich = document.getDate("ngayGiaoDich");

        if (loai == null) {
            loai = "Không xác định";
        }

        if (danhMuc == null || danhMuc.isEmpty()) {
            danhMuc = "Không có danh mục";
        }

        if (ghiChu == null || ghiChu.trim().isEmpty()) {
            ghiChu = "Không có";
        }

        if (soTien == null) {
            soTien = 0L;
        }

        tvLoaiGiaoDichChiTiet.setText(loai.toUpperCase(
                new Locale("vi", "VN")
        ));

        tvLoaiChiTiet.setText("Loại giao dịch: " + loai);

        tvDanhMucChiTietGiaoDich.setText(danhMuc);

        if ("Thu nhập".equals(loai)) {

            tvSoTienChiTietGiaoDich.setText(
                    "+" + dinhDangTien.format(soTien) + " ₫"
            );

            tvSoTienChiTietGiaoDich.setTextColor(
                    Color.parseColor("#059669")
            );

        } else if ("Chi tiêu".equals(loai)) {

            tvSoTienChiTietGiaoDich.setText(
                    "-" + dinhDangTien.format(soTien) + " ₫"
            );

            tvSoTienChiTietGiaoDich.setTextColor(
                    Color.parseColor("#DC2626")
            );

        } else {

            tvSoTienChiTietGiaoDich.setText(
                    dinhDangTien.format(soTien) + " ₫"
            );

            tvSoTienChiTietGiaoDich.setTextColor(
                    Color.parseColor("#0891B2")
            );
        }

        if ("Chuyển tiền".equals(loai)) {
            tvViNguonChiTiet.setText("Ví chuyển: Đang tải...");
            tvViDichChiTiet.setVisibility(View.VISIBLE);
            tvViDichChiTiet.setText("Ví nhận: Đang tải...");
        } else {
            tvViNguonChiTiet.setText("Ví sử dụng: Đang tải...");
            tvViDichChiTiet.setVisibility(View.GONE);
        }

        taiTenVi(maViNguon, tvViNguonChiTiet,
                "Chuyển tiền".equals(loai) ? "Ví chuyển" : "Ví sử dụng");

        if ("Chuyển tiền".equals(loai)) {
            taiTenVi(maViDich, tvViDichChiTiet, "Ví nhận");
        }

        if (ngayGiaoDich != null) {
            tvNgayChiTietGiaoDich.setText(
                    "Ngày giao dịch: " + dinhDangNgay.format(ngayGiaoDich)
            );
        } else {
            tvNgayChiTietGiaoDich.setText("Ngày giao dịch: Không có");
        }

        tvGhiChuChiTietGiaoDich.setText("Ghi chú: " + ghiChu);
    }

    private void taiTenVi(String maVi, TextView textView, String nhan) {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null || maVi == null || maVi.isEmpty()) {
            textView.setText(nhan + ": Không xác định");
            return;
        }

        firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("wallets")
                .document(maVi)
                .get()
                .addOnSuccessListener(document -> {

                    String tenVi = document.getString("tenVi");

                    if (!document.exists()) {
                        tenVi = "Ví không còn tồn tại";
                    } else if (tenVi == null) {
                        tenVi = "Ví chưa đặt tên";
                    }

                    textView.setText(nhan + ": " + tenVi);
                })
                .addOnFailureListener(e -> {
                    textView.setText(nhan + ": Không thể tải");
                });
    }

    private void xacNhanXoaGiaoDich() {

        new AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch")
                .setMessage(
                        "Bạn có chắc chắn muốn xóa giao dịch này?\n\n"
                                + "Phinova sẽ hoàn tác số dư của các ví liên quan "
                                + "và lưu lịch sử xóa.\n\n"
                                + "Thao tác này không thể hoàn tác trực tiếp."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa giao dịch",
                        (dialog, which) -> thucHienXoaGiaoDich())
                .show();
    }

    private void thucHienXoaGiaoDich() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = nguoiDung.getUid();

        DocumentReference giaoDichRef = firestore
                .collection("users")
                .document(uid)
                .collection("transactions")
                .document(maGiaoDich);

        DocumentReference lichSuXoaRef = firestore
                .collection("users")
                .document(uid)
                .collection("transaction_deletions")
                .document();

        btnXoaGiaoDich.setEnabled(false);
        btnSuaGiaoDich.setEnabled(false);

        giaoDichRef.get(Source.SERVER)
                .addOnSuccessListener(duLieuBanDau -> {

                    if (!duLieuBanDau.exists()) {
                        thongBaoXoa("Giao dịch không tồn tại");
                        taiChiTietGiaoDich();
                        return;
                    }

                    String maNguon =
                            duLieuBanDau.getString("maViNguon");

                    String maDich =
                            duLieuBanDau.getString("maViDich");

                    String loaiBanDau =
                            duLieuBanDau.getString("loaiGiaoDich");

                    if (maNguon == null || maNguon.isEmpty()) {
                        thongBaoXoa("Giao dịch thiếu thông tin ví");
                        taiChiTietGiaoDich();
                        return;
                    }

                    boolean laChuyenTien =
                            "Chuyển tiền".equals(loaiBanDau);

                    if (laChuyenTien && (maDich == null
                            || maDich.isEmpty()
                            || maNguon.equals(maDich))) {

                        thongBaoXoa("Thông tin ví nhận không hợp lệ");
                        taiChiTietGiaoDich();
                        return;
                    }

                    DocumentReference viNguonRef = firestore
                            .collection("users")
                            .document(uid)
                            .collection("wallets")
                            .document(maNguon);

                    DocumentReference viDichRef = laChuyenTien
                            ? firestore.collection("users")
                            .document(uid)
                            .collection("wallets")
                            .document(maDich)
                            : null;

                    firestore.runTransaction(transaction -> {

                        com.google.firebase.firestore.DocumentSnapshot giaoDich =
                                transaction.get(giaoDichRef);

                        if (!giaoDich.exists()) {
                            throw new IllegalStateException(
                                    "Giao dịch đã bị xóa"
                            );
                        }

                        String loai =
                                giaoDich.getString("loaiGiaoDich");

                        String nguon =
                                giaoDich.getString("maViNguon");

                        String dich =
                                giaoDich.getString("maViDich");

                        Long soTien =
                                giaoDich.getLong("soTien");

                        if (!"Thu nhập".equals(loai)
                                && !"Chi tiêu".equals(loai)
                                && !"Chuyển tiền".equals(loai)) {
                            throw new IllegalStateException(
                                    "Loại giao dịch không hỗ trợ xóa"
                            );
                        }

                        if (soTien == null || soTien <= 0) {
                            throw new IllegalStateException(
                                    "Số tiền giao dịch không hợp lệ"
                            );
                        }

                        if (!maNguon.equals(nguon)
                                || !java.util.Objects.equals(maDich, dich)
                                || !loaiBanDau.equals(loai)) {
                            throw new IllegalStateException(
                                    "Giao dịch đã thay đổi. "
                                            + "Vui lòng mở lại để kiểm tra"
                            );
                        }

                        com.google.firebase.firestore.DocumentSnapshot viNguon =
                                transaction.get(viNguonRef);

                        if (!viNguon.exists()) {
                            throw new IllegalStateException(
                                    "Ví nguồn không tồn tại"
                            );
                        }

                        String trangThaiNguon =
                                viNguon.getString("trangThai");

                        if (trangThaiNguon != null
                                && !"HOAT_DONG".equals(trangThaiNguon)) {
                            throw new IllegalStateException(
                                    "Ví nguồn đã ngừng hoạt động"
                            );
                        }

                        Long soDuNguon =
                                viNguon.getLong("soDu");

                        if (soDuNguon == null || soDuNguon < 0) {
                            throw new IllegalStateException(
                                    "Số dư ví nguồn không hợp lệ"
                            );
                        }

                        Long soDuDich = null;

                        if ("Chuyển tiền".equals(loai)) {

                            com.google.firebase.firestore.DocumentSnapshot viDich =
                                    transaction.get(viDichRef);

                            if (!viDich.exists()) {
                                throw new IllegalStateException(
                                        "Ví nhận không tồn tại"
                                );
                            }

                            String trangThaiDich =
                                    viDich.getString("trangThai");

                            if (trangThaiDich != null
                                    && !"HOAT_DONG".equals(trangThaiDich)) {
                                throw new IllegalStateException(
                                        "Ví nhận đã ngừng hoạt động"
                                );
                            }

                            soDuDich = viDich.getLong("soDu");

                            if (soDuDich == null || soDuDich < 0) {
                                throw new IllegalStateException(
                                        "Số dư ví nhận không hợp lệ"
                                );
                            }
                        }

                        long soDuNguonMoi;

                        if ("Thu nhập".equals(loai)) {

                            soDuNguonMoi =
                                    Math.subtractExact(soDuNguon, soTien);

                            if (soDuNguonMoi < 0) {
                                throw new IllegalStateException(
                                        "Số dư không đủ để hoàn tác thu nhập"
                                );
                            }

                            transaction.update(
                                    viNguonRef, "soDu", soDuNguonMoi
                            );

                        } else if ("Chi tiêu".equals(loai)) {

                            soDuNguonMoi =
                                    Math.addExact(soDuNguon, soTien);

                            transaction.update(
                                    viNguonRef, "soDu", soDuNguonMoi
                            );

                        } else {

                            soDuNguonMoi =
                                    Math.addExact(soDuNguon, soTien);

                            long soDuDichMoi =
                                    Math.subtractExact(soDuDich, soTien);

                            if (soDuDichMoi < 0) {
                                throw new IllegalStateException(
                                        "Ví nhận không đủ số dư để hoàn tác"
                                );
                            }

                            transaction.update(
                                    viNguonRef, "soDu", soDuNguonMoi
                            );

                            transaction.update(
                                    viDichRef, "soDu", soDuDichMoi
                            );
                        }

                        Map<String, Object> lichSu = new HashMap<>();

                        lichSu.put("maGiaoDich", maGiaoDich);
                        lichSu.put("loaiGiaoDich", loai);
                        lichSu.put("soTien", soTien);
                        lichSu.put("maViNguon", nguon);
                        lichSu.put("maViDich", dich);
                        lichSu.put("danhMuc",
                                giaoDich.getString("danhMuc"));
                        lichSu.put("ghiChu",
                                giaoDich.getString("ghiChu"));
                        lichSu.put("ngayGiaoDich",
                                giaoDich.getDate("ngayGiaoDich"));
                        lichSu.put("soDuNguonSauXoa", soDuNguonMoi);
                        lichSu.put("ngayXoa", FieldValue.serverTimestamp());

                        transaction.set(lichSuXoaRef, lichSu);
                        transaction.delete(giaoDichRef);

                        return null;

                    }).addOnSuccessListener(unused -> {

                        Toast.makeText(this,
                                "Xóa giao dịch thành công!",
                                Toast.LENGTH_SHORT).show();

                        finish();

                    }).addOnFailureListener(e -> {

                        thongBaoXoa(
                                "Không thể xóa giao dịch: "
                                        + e.getMessage()
                        );

                        taiChiTietGiaoDich();
                    });
                })
                .addOnFailureListener(e -> {

                    thongBaoXoa(
                            "Không thể kiểm tra giao dịch: "
                                    + e.getMessage()
                    );

                    taiChiTietGiaoDich();
                });
    }

    private void thongBaoXoa(String noiDung) {
        Toast.makeText(this, noiDung, Toast.LENGTH_LONG).show();
    }

}
