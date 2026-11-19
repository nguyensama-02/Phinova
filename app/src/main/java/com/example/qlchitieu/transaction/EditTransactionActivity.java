package com.example.qlchitieu.transaction;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qlchitieu.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EditTransactionActivity extends AppCompatActivity {

    TextView tvQuayLaiSuaGiaoDich;
    TextView tvThongTinGiaoDichSua, tvSoTienCuSua;
    TextView tvNhanDanhMucSua, tvNgaySuaGiaoDich;

    EditText edtSoTienSua, edtGhiChuSua;
    Spinner spDanhMucSua;
    MaterialButton btnLuuSuaGiaoDich;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    String maGiaoDich;
    String loaiGiaoDichCu;
    String maViNguonCu, maViDichCu;
    String danhMucCu, ghiChuCu;

    long soTienCu;
    Date ngayGiaoDichCu;

    Calendar ngayDuocChon = Calendar.getInstance();

    boolean daTaiGiaoDich = false;
    boolean dangLuu = false;

    ArrayList<String> danhSachDanhMuc = new ArrayList<>();

    NumberFormat dinhDangTien =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    SimpleDateFormat dinhDangNgay =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        tvQuayLaiSuaGiaoDich =
                findViewById(R.id.tvQuayLaiSuaGiaoDich);

        tvThongTinGiaoDichSua =
                findViewById(R.id.tvThongTinGiaoDichSua);

        tvSoTienCuSua = findViewById(R.id.tvSoTienCuSua);
        tvNhanDanhMucSua = findViewById(R.id.tvNhanDanhMucSua);
        tvNgaySuaGiaoDich = findViewById(R.id.tvNgaySuaGiaoDich);

        edtSoTienSua = findViewById(R.id.edtSoTienSua);
        edtGhiChuSua = findViewById(R.id.edtGhiChuSua);

        spDanhMucSua = findViewById(R.id.spDanhMucSua);

        btnLuuSuaGiaoDich = findViewById(R.id.btnLuuSuaGiaoDich);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        maGiaoDich = getIntent().getStringExtra("maGiaoDich");

        btnLuuSuaGiaoDich.setEnabled(false);

        tvQuayLaiSuaGiaoDich.setOnClickListener(v -> finish());

        tvNgaySuaGiaoDich.setOnClickListener(v -> chonNgay());

        btnLuuSuaGiaoDich.setOnClickListener(v -> luuChinhSua());

        if (maGiaoDich == null || maGiaoDich.trim().isEmpty()) {
            thongBao("Không tìm thấy mã giao dịch");
            finish();
            return;
        }

        taiThongTinGiaoDich();
    }

    private DocumentReference layGiaoDichReference() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            return null;
        }

        return firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("transactions")
                .document(maGiaoDich);
    }

    private void taiThongTinGiaoDich() {

        DocumentReference giaoDichRef = layGiaoDichReference();

        if (giaoDichRef == null) {
            thongBao("Vui lòng đăng nhập lại");
            finish();
            return;
        }

        btnLuuSuaGiaoDich.setEnabled(false);

        giaoDichRef.get(Source.SERVER)
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        thongBao("Giao dịch không tồn tại");
                        finish();
                        return;
                    }

                    String loai = document.getString("loaiGiaoDich");
                    Long soTien = document.getLong("soTien");

                    String maNguon = document.getString("maViNguon");
                    String maDich = document.getString("maViDich");

                    if (!"Thu nhập".equals(loai)
                            && !"Chi tiêu".equals(loai)
                            && !"Chuyển tiền".equals(loai)) {

                        thongBao("Loại giao dịch không hỗ trợ chỉnh sửa");
                        finish();
                        return;
                    }

                    if (soTien == null || soTien <= 0
                            || maNguon == null
                            || maNguon.isEmpty()
                            || ("Chuyển tiền".equals(loai)
                            && (maDich == null
                            || maDich.isEmpty()
                            || maNguon.equals(maDich)))) {

                        thongBao("Dữ liệu giao dịch không hợp lệ");
                        finish();
                        return;
                    }

                    loaiGiaoDichCu = loai;
                    soTienCu = soTien;

                    maViNguonCu = maNguon;
                    maViDichCu = maDich;

                    danhMucCu = document.getString("danhMuc");
                    ghiChuCu = document.getString("ghiChu");

                    if (danhMucCu == null) {
                        danhMucCu = "";
                    }

                    if (ghiChuCu == null) {
                        ghiChuCu = "";
                    }

                    ngayGiaoDichCu = document.getDate("ngayGiaoDich");

                    if (ngayGiaoDichCu == null) {
                        thongBao("Ngày giao dịch không hợp lệ");
                        finish();
                        return;
                    }

                    ngayDuocChon.setTime(ngayGiaoDichCu);

                    edtSoTienSua.setText(String.valueOf(soTienCu));
                    edtGhiChuSua.setText(ghiChuCu);

                    tvSoTienCuSua.setText(
                            dinhDangTien.format(soTienCu) + " ₫"
                    );

                    tvThongTinGiaoDichSua.setText(loaiGiaoDichCu);

                    tvNgaySuaGiaoDich.setText(
                            dinhDangNgay.format(ngayGiaoDichCu)
                    );

                    hienThiDanhMuc();

                    daTaiGiaoDich = true;
                    btnLuuSuaGiaoDich.setEnabled(true);

                })
                .addOnFailureListener(e -> {
                    thongBao("Không thể tải giao dịch: "
                            + e.getMessage());
                });
    }

    private void hienThiDanhMuc() {

        danhSachDanhMuc.clear();

        if ("Chuyển tiền".equals(loaiGiaoDichCu)) {

            tvNhanDanhMucSua.setVisibility(View.GONE);
            spDanhMucSua.setVisibility(View.GONE);
            return;
        }

        tvNhanDanhMucSua.setVisibility(View.VISIBLE);
        spDanhMucSua.setVisibility(View.VISIBLE);

        String[] danhMuc;

        if ("Thu nhập".equals(loaiGiaoDichCu)) {
            danhMuc = new String[]{
                    "Tiền lương",
                    "Tiền thưởng",
                    "Kinh doanh",
                    "Quà tặng",
                    "Thu nhập khác"
            };
        } else {
            danhMuc = new String[]{
                    "Ăn uống",
                    "Mua sắm",
                    "Đi lại",
                    "Hóa đơn",
                    "Giải trí",
                    "Học tập",
                    "Sức khỏe",
                    "Chi tiêu khác"
            };
        }

        for (String muc : danhMuc) {
            danhSachDanhMuc.add(muc);
        }

        if (!danhMucCu.isEmpty()
                && !danhSachDanhMuc.contains(danhMucCu)) {
            danhSachDanhMuc.add(danhMucCu);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                danhSachDanhMuc
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spDanhMucSua.setAdapter(adapter);

        int viTri = danhSachDanhMuc.indexOf(danhMucCu);

        if (viTri >= 0) {
            spDanhMucSua.setSelection(viTri);
        }
    }

    private void chonNgay() {

        if (!daTaiGiaoDich || dangLuu) {
            return;
        }

        int ngay = ngayDuocChon.get(Calendar.DAY_OF_MONTH);
        int thang = ngayDuocChon.get(Calendar.MONTH);
        int nam = ngayDuocChon.get(Calendar.YEAR);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, namMoi, thangMoi, ngayMoi) -> {

                    ngayDuocChon.set(
                            namMoi,
                            thangMoi,
                            ngayMoi
                    );

                    tvNgaySuaGiaoDich.setText(
                            dinhDangNgay.format(ngayDuocChon.getTime())
                    );
                },
                nam,
                thang,
                ngay
        );

        dialog.show();
    }

    private boolean viDangHoatDong(DocumentSnapshot document) {

        String trangThai = document.getString("trangThai");

        return trangThai == null
                || "HOAT_DONG".equals(trangThai);
    }

    private void luuChinhSua() {

        if (!daTaiGiaoDich || dangLuu) {
            return;
        }

        String soTienNhap =
                edtSoTienSua.getText().toString().trim();

        String ghiChuMoi =
                edtGhiChuSua.getText().toString().trim();

        if (soTienNhap.isEmpty()) {
            edtSoTienSua.setError("Vui lòng nhập số tiền");
            return;
        }

        long soTienMoi;

        try {
            soTienMoi = Long.parseLong(soTienNhap);
        } catch (NumberFormatException e) {
            edtSoTienSua.setError("Số tiền không hợp lệ");
            return;
        }

        if (soTienMoi <= 0) {
            edtSoTienSua.setError("Số tiền phải lớn hơn 0");
            return;
        }

        String danhMucMoi = "";

        if (!"Chuyển tiền".equals(loaiGiaoDichCu)) {

            if (spDanhMucSua.getSelectedItem() == null) {
                thongBao("Vui lòng chọn danh mục");
                return;
            }

            danhMucMoi = spDanhMucSua.getSelectedItem().toString();
        }

        Date ngayMoi = ngayDuocChon.getTime();

        boolean khongThayDoi =
                soTienMoi == soTienCu
                        && Objects.equals(danhMucMoi, danhMucCu)
                        && Objects.equals(ghiChuMoi, ghiChuCu)
                        && ngayMoi.equals(ngayGiaoDichCu);

        if (khongThayDoi) {
            thongBao("Bạn chưa thay đổi thông tin nào");
            return;
        }

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            thongBao("Vui lòng đăng nhập lại");
            return;
        }

        String uid = nguoiDung.getUid();

        DocumentReference giaoDichRef = layGiaoDichReference();

        DocumentReference viNguonRef = firestore
                .collection("users")
                .document(uid)
                .collection("wallets")
                .document(maViNguonCu);

        DocumentReference viDichRef = null;

        if ("Chuyển tiền".equals(loaiGiaoDichCu)) {

            viDichRef = firestore
                    .collection("users")
                    .document(uid)
                    .collection("wallets")
                    .document(maViDichCu);
        }

        DocumentReference lichSuRef = firestore
                .collection("users")
                .document(uid)
                .collection("transaction_edits")
                .document();

        String loaiCuoi = loaiGiaoDichCu;
        String danhMucCuoi = danhMucMoi;
        String ghiChuCuoi = ghiChuMoi;
        DocumentReference viDichCuoiRef = viDichRef;

        dangLuu = true;
        btnLuuSuaGiaoDich.setEnabled(false);

        firestore.runTransaction(transaction -> {

            DocumentSnapshot giaoDichHienTai =
                    transaction.get(giaoDichRef);

            if (!giaoDichHienTai.exists()) {
                throw new IllegalStateException(
                        "Giao dịch không còn tồn tại"
                );
            }

            String loaiHienTai =
                    giaoDichHienTai.getString("loaiGiaoDich");

            Long soTienHienTai =
                    giaoDichHienTai.getLong("soTien");

            String nguonHienTai =
                    giaoDichHienTai.getString("maViNguon");

            String dichHienTai =
                    giaoDichHienTai.getString("maViDich");

            String danhMucHienTai =
                    giaoDichHienTai.getString("danhMuc");

            String ghiChuHienTai =
                    giaoDichHienTai.getString("ghiChu");

            Date ngayHienTai =
                    giaoDichHienTai.getDate("ngayGiaoDich");

            if (danhMucHienTai == null) {
                danhMucHienTai = "";
            }

            if (ghiChuHienTai == null) {
                ghiChuHienTai = "";
            }

            if (!Objects.equals(loaiHienTai, loaiCuoi)
                    || !Objects.equals(soTienHienTai, soTienCu)
                    || !Objects.equals(nguonHienTai, maViNguonCu)
                    || !Objects.equals(dichHienTai, maViDichCu)
                    || !Objects.equals(danhMucHienTai, danhMucCu)
                    || !Objects.equals(ghiChuHienTai, ghiChuCu)
                    || !Objects.equals(ngayHienTai, ngayGiaoDichCu)) {

                throw new IllegalStateException(
                        "Giao dịch đã được thay đổi. "
                                + "Vui lòng mở lại để cập nhật dữ liệu"
                );
            }

            DocumentSnapshot duLieuNguon =
                    transaction.get(viNguonRef);

            if (!duLieuNguon.exists()) {
                throw new IllegalStateException(
                        "Ví liên quan không tồn tại"
                );
            }

            if (!viDangHoatDong(duLieuNguon)) {
                throw new IllegalStateException(
                        "Ví đã lưu trữ, không thể chỉnh sửa"
                );
            }

            Long soDuNguon = duLieuNguon.getLong("soDu");

            if (soDuNguon == null || soDuNguon < 0) {
                throw new IllegalStateException(
                        "Số dư ví không hợp lệ"
                );
            }

            Long soDuDich = null;

            if ("Chuyển tiền".equals(loaiCuoi)) {

                DocumentSnapshot duLieuDich =
                        transaction.get(viDichCuoiRef);

                if (!duLieuDich.exists()) {
                    throw new IllegalStateException(
                            "Ví nhận không tồn tại"
                    );
                }

                if (!viDangHoatDong(duLieuDich)) {
                    throw new IllegalStateException(
                            "Ví nhận đã lưu trữ"
                    );
                }

                soDuDich = duLieuDich.getLong("soDu");

                if (soDuDich == null || soDuDich < 0) {
                    throw new IllegalStateException(
                            "Số dư ví nhận không hợp lệ"
                    );
                }
            }

            long chenhLech =
                    Math.subtractExact(soTienMoi, soTienCu);

            if ("Thu nhập".equals(loaiCuoi)) {

                long soDuMoi =
                        Math.addExact(soDuNguon, chenhLech);

                if (soDuMoi < 0) {
                    throw new IllegalStateException(
                            "Không đủ số dư để giảm khoản thu nhập"
                    );
                }

                transaction.update(
                        viNguonRef, "soDu", soDuMoi
                );

            } else if ("Chi tiêu".equals(loaiCuoi)) {

                long soDuMoi =
                        Math.subtractExact(soDuNguon, chenhLech);

                if (soDuMoi < 0) {
                    throw new IllegalStateException(
                            "Không đủ số dư để tăng khoản chi tiêu"
                    );
                }

                transaction.update(
                        viNguonRef, "soDu", soDuMoi
                );

            } else if ("Chuyển tiền".equals(loaiCuoi)) {

                long soDuNguonMoi =
                        Math.subtractExact(soDuNguon, chenhLech);

                long soDuDichMoi =
                        Math.addExact(soDuDich, chenhLech);

                if (soDuNguonMoi < 0 || soDuDichMoi < 0) {
                    throw new IllegalStateException(
                            "Số dư một trong hai ví không đủ"
                    );
                }

                transaction.update(
                        viNguonRef, "soDu", soDuNguonMoi
                );

                transaction.update(
                        viDichCuoiRef, "soDu", soDuDichMoi
                );
            }

            Map<String, Object> capNhat = new HashMap<>();

            capNhat.put("soTien", soTienMoi);
            capNhat.put("danhMuc", danhMucCuoi);
            capNhat.put("ghiChu", ghiChuCuoi);
            capNhat.put("ngayGiaoDich", ngayMoi);
            capNhat.put("ngayCapNhat", FieldValue.serverTimestamp());

            transaction.update(giaoDichRef, capNhat);

            Map<String, Object> lichSu = new HashMap<>();

            lichSu.put("maGiaoDich", maGiaoDich);
            lichSu.put("loaiGiaoDich", loaiCuoi);
            lichSu.put("maViNguon", maViNguonCu);
            lichSu.put("maViDich", maViDichCu);

            lichSu.put("soTienCu", soTienCu);
            lichSu.put("soTienMoi", soTienMoi);

            lichSu.put("danhMucCu", danhMucCu);
            lichSu.put("danhMucMoi", danhMucCuoi);

            lichSu.put("ghiChuCu", ghiChuCu);
            lichSu.put("ghiChuMoi", ghiChuCuoi);

            lichSu.put("ngayGiaoDichCu", ngayGiaoDichCu);
            lichSu.put("ngayGiaoDichMoi", ngayMoi);

            lichSu.put(
                    "ngayChinhSua",
                    FieldValue.serverTimestamp()
            );

            transaction.set(lichSuRef, lichSu);

            return null;

        }).addOnSuccessListener(unused -> {

            dangLuu = false;

            Toast.makeText(this,
                    "Chỉnh sửa giao dịch thành công!",
                    Toast.LENGTH_SHORT).show();

            finish();

        }).addOnFailureListener(e -> {

            dangLuu = false;
            btnLuuSuaGiaoDich.setEnabled(true);

            thongBao("Không thể chỉnh sửa: " + e.getMessage());
        });
    }

    private void thongBao(String noiDung) {
        Toast.makeText(this, noiDung, Toast.LENGTH_LONG).show();
    }
}
