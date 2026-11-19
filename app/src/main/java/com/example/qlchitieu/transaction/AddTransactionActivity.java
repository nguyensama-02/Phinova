package com.example.qlchitieu.transaction;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qlchitieu.R;
import com.example.qlchitieu.model.Wallet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    MaterialButtonToggleGroup toggleLoaiGiaoDich;

    LinearLayout layoutViDich, layoutDanhMuc;

    TextView tvQuayLaiGiaoDich, tvNhanViNguon;
    TextView tvNgayGiaoDich;

    Spinner spViNguon, spViDich, spDanhMuc;

    EditText edtSoTienGiaoDich, edtGhiChu;
    MaterialButton btnLuuGiaoDich;

    ArrayList<Wallet> danhSachVi;
    ArrayList<String> danhSachTenVi;

    ArrayAdapter<String> adapterVi;

    Calendar ngayDuocChon;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    boolean dangTaiVi = false;
    boolean dangLuuGiaoDich = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        spViNguon = findViewById(R.id.spViNguon);
        spViDich = findViewById(R.id.spViDich);
        spDanhMuc = findViewById(R.id.spDanhMuc);

        tvNgayGiaoDich = findViewById(R.id.tvNgayGiaoDich);
        tvQuayLaiGiaoDich = findViewById(R.id.tvQuayLaiGiaoDich);
        tvNhanViNguon = findViewById(R.id.tvNhanViNguon);

        edtSoTienGiaoDich = findViewById(R.id.edtSoTienGiaoDich);
        edtGhiChu = findViewById(R.id.edtGhiChu);

        btnLuuGiaoDich = findViewById(R.id.btnLuuGiaoDich);
        toggleLoaiGiaoDich = findViewById(R.id.toggleLoaiGiaoDich);

        layoutViDich = findViewById(R.id.layoutViDich);
        layoutDanhMuc = findViewById(R.id.layoutDanhMuc);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        danhSachVi = new ArrayList<>();
        danhSachTenVi = new ArrayList<>();

        adapterVi = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                danhSachTenVi
        );

        adapterVi.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spViNguon.setAdapter(adapterVi);
        spViDich.setAdapter(adapterVi);

        ngayDuocChon = Calendar.getInstance();

        hienThiNgay();
        taiDanhMuc("Chi tiêu");

        tvQuayLaiGiaoDich.setOnClickListener(v -> finish());

        tvNgayGiaoDich.setOnClickListener(v -> chonNgay());

        btnLuuGiaoDich.setOnClickListener(v -> luuGiaoDich());

        toggleLoaiGiaoDich.addOnButtonCheckedListener(
                (group, checkedId, isChecked) -> {

                    if (!isChecked) {
                        return;
                    }

                    if (checkedId == R.id.btnChuyenTien) {

                        layoutViDich.setVisibility(View.VISIBLE);
                        layoutDanhMuc.setVisibility(View.GONE);

                        tvNhanViNguon.setText("Ví chuyển tiền");

                    } else {

                        layoutViDich.setVisibility(View.GONE);
                        layoutDanhMuc.setVisibility(View.VISIBLE);

                        tvNhanViNguon.setText("Ví sử dụng");

                        if (checkedId == R.id.btnThuNhap) {
                            taiDanhMuc("Thu nhập");
                        } else {
                            taiDanhMuc("Chi tiêu");
                        }
                    }

                    capNhatNutLuu();
                }
        );

        btnLuuGiaoDich.setEnabled(false);
        capNhatNutLuu();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!dangLuuGiaoDich) {
            taiDanhSachVi();
        }
    }

    private void capNhatNutLuu() {

        if (btnLuuGiaoDich == null) {
            return;
        }

        boolean coVi = danhSachVi != null
                && !danhSachVi.isEmpty();

        btnLuuGiaoDich.setEnabled(
                coVi && !dangTaiVi && !dangLuuGiaoDich
        );
    }

    private void taiDanhSachVi() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dangTaiVi = true;

        danhSachVi.clear();
        danhSachTenVi.clear();
        adapterVi.notifyDataSetChanged();

        capNhatNutLuu();

        firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("wallets")
                .get(Source.SERVER)
                .addOnSuccessListener(querySnapshot -> {

                    danhSachVi.clear();
                    danhSachTenVi.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String trangThai =
                                document.getString("trangThai");

                        if ("LUU_TRU".equals(trangThai)) {
                            continue;
                        }

                        if (trangThai != null
                                && !"HOAT_DONG".equals(trangThai)) {
                            continue;
                        }

                        String maVi = document.getId();
                        String tenVi = document.getString("tenVi");
                        String loaiVi = document.getString("loaiVi");
                        Long soDu = document.getLong("soDu");

                        if (tenVi == null) {
                            tenVi = "Ví chưa đặt tên";
                        }

                        if (loaiVi == null) {
                            loaiVi = "Khác";
                        }

                        if (soDu == null) {
                            soDu = 0L;
                        }

                        Wallet vi = new Wallet(
                                maVi,
                                tenVi,
                                loaiVi,
                                soDu
                        );

                        vi.setTrangThai("HOAT_DONG");

                        danhSachVi.add(vi);
                        danhSachTenVi.add(tenVi);
                    }

                    adapterVi.notifyDataSetChanged();

                    dangTaiVi = false;
                    capNhatNutLuu();

                    if (danhSachVi.isEmpty()) {

                        Toast.makeText(this,
                                "Không có ví đang sử dụng. "
                                        + "Hãy tạo hoặc khôi phục ví trước!",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {

                    dangTaiVi = false;
                    capNhatNutLuu();

                    Toast.makeText(this,
                            "Không thể tải danh sách ví: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void taiDanhMuc(String loaiGiaoDich) {

        String[] danhMuc;

        if (loaiGiaoDich.equals("Thu nhập")) {

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

        ArrayAdapter<String> adapterDanhMuc = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                danhMuc
        );

        adapterDanhMuc.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spDanhMuc.setAdapter(adapterDanhMuc);
    }

    private void hienThiNgay() {

        int ngay = ngayDuocChon.get(Calendar.DAY_OF_MONTH);
        int thang = ngayDuocChon.get(Calendar.MONTH) + 1;
        int nam = ngayDuocChon.get(Calendar.YEAR);

        String ngayHienThi = String.format(
                Locale.forLanguageTag("vi-VN"),
                "%02d/%02d/%04d",
                ngay, thang, nam
        );

        tvNgayGiaoDich.setText(ngayHienThi);
    }

    private void chonNgay() {

        int ngay = ngayDuocChon.get(Calendar.DAY_OF_MONTH);
        int thang = ngayDuocChon.get(Calendar.MONTH);
        int nam = ngayDuocChon.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, namChon, thangChon, ngayChon) -> {

                    ngayDuocChon.set(
                            namChon,
                            thangChon,
                            ngayChon
                    );

                    hienThiNgay();
                },
                nam,
                thang,
                ngay
        );

        datePickerDialog.show();
    }

    private void luuGiaoDich() {

        if (dangTaiVi || dangLuuGiaoDich) {
            return;
        }

        String soTienNhap = edtSoTienGiaoDich
                .getText().toString().trim();

        String ghiChu = edtGhiChu
                .getText().toString().trim();

        if (soTienNhap.isEmpty()) {
            edtSoTienGiaoDich.setError("Vui lòng nhập số tiền");
            edtSoTienGiaoDich.requestFocus();
            return;
        }

        long soTien;

        try {
            soTien = Long.parseLong(soTienNhap);
        } catch (NumberFormatException e) {
            edtSoTienGiaoDich.setError("Số tiền không hợp lệ");
            return;
        }

        if (soTien <= 0) {
            edtSoTienGiaoDich.setError("Số tiền phải lớn hơn 0");
            return;
        }

        if (danhSachVi.isEmpty()) {
            Toast.makeText(this,
                    "Không có ví đang sử dụng",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int viTriNguon = spViNguon.getSelectedItemPosition();

        if (viTriNguon < 0 || viTriNguon >= danhSachVi.size()) {
            Toast.makeText(this,
                    "Vui lòng chọn ví",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Wallet viNguon = danhSachVi.get(viTriNguon);

        int nutDuocChon =
                toggleLoaiGiaoDich.getCheckedButtonId();

        String loaiGiaoDich;

        if (nutDuocChon == R.id.btnThuNhap) {
            loaiGiaoDich = "Thu nhập";
        } else if (nutDuocChon == R.id.btnChuyenTien) {
            loaiGiaoDich = "Chuyển tiền";
        } else {
            loaiGiaoDich = "Chi tiêu";
        }

        Wallet viDich = null;
        String danhMuc = "";

        if (loaiGiaoDich.equals("Chuyển tiền")) {

            if (danhSachVi.size() < 2) {
                Toast.makeText(this,
                        "Cần ít nhất 2 ví đang sử dụng để chuyển tiền",
                        Toast.LENGTH_LONG).show();
                return;
            }

            int viTriDich = spViDich.getSelectedItemPosition();

            if (viTriDich < 0
                    || viTriDich >= danhSachVi.size()) {

                Toast.makeText(this,
                        "Vui lòng chọn ví nhận",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            viDich = danhSachVi.get(viTriDich);

            if (viNguon.getMaVi().equals(viDich.getMaVi())) {
                Toast.makeText(this,
                        "Ví chuyển và ví nhận phải khác nhau",
                        Toast.LENGTH_SHORT).show();
                return;
            }

        } else {

            if (spDanhMuc.getSelectedItem() == null) {
                Toast.makeText(this,
                        "Vui lòng chọn danh mục",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            danhMuc = spDanhMuc.getSelectedItem().toString();
        }

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference viNguonRef = firestore
                .collection("users")
                .document(nguoiDung.getUid())
                .collection("wallets")
                .document(viNguon.getMaVi());

        DocumentReference viDichRef = null;

        if (viDich != null) {
            viDichRef = firestore
                    .collection("users")
                    .document(nguoiDung.getUid())
                    .collection("wallets")
                    .document(viDich.getMaVi());
        }

        DocumentReference giaoDichRef = firestore
                .collection("users")
                .document(nguoiDung.getUid())
                .collection("transactions")
                .document();

        Map<String, Object> giaoDich = new HashMap<>();

        giaoDich.put("loaiGiaoDich", loaiGiaoDich);
        giaoDich.put("soTien", soTien);
        giaoDich.put("maViNguon", viNguon.getMaVi());
        giaoDich.put("maViDich",
                viDich == null ? null : viDich.getMaVi());
        giaoDich.put("danhMuc", danhMuc);
        giaoDich.put("ghiChu", ghiChu);
        giaoDich.put(
                "ngayGiaoDich",
                new Date(ngayDuocChon.getTimeInMillis())
        );
        giaoDich.put("ngayTao", FieldValue.serverTimestamp());

        String loaiCuoi = loaiGiaoDich;
        DocumentReference viDichCuoiRef = viDichRef;

        dangLuuGiaoDich = true;
        capNhatNutLuu();

        firestore.runTransaction(transaction -> {

            DocumentSnapshot duLieuNguon =
                    transaction.get(viNguonRef);

            if (!duLieuNguon.exists()) {
                throw new IllegalStateException(
                        "Ví nguồn không tồn tại"
                );
            }

            String trangThaiNguon =
                    duLieuNguon.getString("trangThai");

            if ("LUU_TRU".equals(trangThaiNguon)) {
                throw new IllegalStateException(
                        "Ví nguồn đã lưu trữ, không thể giao dịch"
                );
            }

            if (trangThaiNguon != null
                    && !"HOAT_DONG".equals(trangThaiNguon)) {
                throw new IllegalStateException(
                        "Trạng thái ví nguồn không hợp lệ"
                );
            }

            Long soDuNguon = duLieuNguon.getLong("soDu");

            if (soDuNguon == null) {
                throw new IllegalStateException(
                        "Số dư ví nguồn không hợp lệ"
                );
            }

            Long soDuDich = null;

            if (loaiCuoi.equals("Chuyển tiền")) {

                DocumentSnapshot duLieuDich =
                        transaction.get(viDichCuoiRef);

                if (!duLieuDich.exists()) {
                    throw new IllegalStateException(
                            "Ví nhận không tồn tại"
                    );
                }

                String trangThaiDich =
                        duLieuDich.getString("trangThai");

                if ("LUU_TRU".equals(trangThaiDich)) {
                    throw new IllegalStateException(
                            "Ví nhận đã lưu trữ, không thể chuyển tiền"
                    );
                }

                if (trangThaiDich != null
                        && !"HOAT_DONG".equals(trangThaiDich)) {
                    throw new IllegalStateException(
                            "Trạng thái ví nhận không hợp lệ"
                    );
                }

                soDuDich = duLieuDich.getLong("soDu");

                if (soDuDich == null) {
                    throw new IllegalStateException(
                            "Số dư ví nhận không hợp lệ"
                    );
                }
            }

            if (loaiCuoi.equals("Thu nhập")) {

                long soDuMoi =
                        Math.addExact(soDuNguon, soTien);

                transaction.update(
                        viNguonRef,
                        "soDu",
                        soDuMoi
                );

            } else if (loaiCuoi.equals("Chi tiêu")) {

                if (soDuNguon < soTien) {
                    throw new IllegalStateException(
                            "Số dư ví không đủ"
                    );
                }

                transaction.update(
                        viNguonRef,
                        "soDu",
                        soDuNguon - soTien
                );

            } else {

                if (soDuNguon < soTien) {
                    throw new IllegalStateException(
                            "Số dư ví nguồn không đủ"
                    );
                }

                long soDuDichMoi =
                        Math.addExact(soDuDich, soTien);

                transaction.update(
                        viNguonRef,
                        "soDu",
                        soDuNguon - soTien
                );

                transaction.update(
                        viDichCuoiRef,
                        "soDu",
                        soDuDichMoi
                );
            }

            transaction.set(giaoDichRef, giaoDich);

            return null;

        }).addOnSuccessListener(unused -> {

            dangLuuGiaoDich = false;

            Toast.makeText(this,
                    "Lưu giao dịch thành công!",
                    Toast.LENGTH_SHORT).show();

            finish();

        }).addOnFailureListener(e -> {

            dangLuuGiaoDich = false;
            capNhatNutLuu();

            Toast.makeText(this,
                    "Không thể lưu giao dịch: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG).show();

            taiDanhSachVi();
        });
    }
}
