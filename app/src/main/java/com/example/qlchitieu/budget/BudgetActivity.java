package com.example.qlchitieu.budget;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlchitieu.model.Budget;
import com.example.qlchitieu.adapter.BudgetAdapter;
import com.example.qlchitieu.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;
import android.widget.EditText;
import android.text.InputType;
import com.google.firebase.firestore.DocumentReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BudgetActivity extends AppCompatActivity {

    TextView tvQuayLaiNganSach, tvThangNganSach;
    TextView tvTongNganSach, tvDaChiNganSach;

    LinearLayout layoutNganSachTrong;
    RecyclerView rvDanhSachNganSach;

    MaterialButton btnThemNganSach;

    ArrayList<Budget> danhSachNganSach;
    BudgetAdapter budgetAdapter;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    int thang, nam;
    int lanTaiDuLieu = 0;

    NumberFormat dinhDangTien =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        tvQuayLaiNganSach =
                findViewById(R.id.tvQuayLaiNganSach);

        tvThangNganSach =
                findViewById(R.id.tvThangNganSach);

        tvTongNganSach =
                findViewById(R.id.tvTongNganSach);

        tvDaChiNganSach =
                findViewById(R.id.tvDaChiNganSach);

        layoutNganSachTrong =
                findViewById(R.id.layoutNganSachTrong);

        rvDanhSachNganSach =
                findViewById(R.id.rvDanhSachNganSach);

        btnThemNganSach =
                findViewById(R.id.btnThemNganSach);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Calendar lich = Calendar.getInstance();

        thang = lich.get(Calendar.MONTH) + 1;
        nam = lich.get(Calendar.YEAR);

        danhSachNganSach = new ArrayList<>();

        budgetAdapter = new BudgetAdapter(danhSachNganSach);

        rvDanhSachNganSach.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvDanhSachNganSach.setAdapter(budgetAdapter);
        budgetAdapter.setOnBudgetClickListener(
                nganSach -> hienThiQuanLyNganSach(nganSach)
        );
        hienThiThang();

        tvQuayLaiNganSach.setOnClickListener(v -> finish());

        tvThangNganSach.setOnClickListener(v -> chonThang());

        btnThemNganSach.setOnClickListener(v -> {
            Intent intent = new Intent(
                    BudgetActivity.this,
                    AddBudgetActivity.class
            );

            intent.putExtra("thang", thang);
            intent.putExtra("nam", nam);

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        taiDuLieuNganSach();
    }

    private void hienThiThang() {

        tvThangNganSach.setText(
                String.format(
                        Locale.forLanguageTag("vi-VN"),
                        "Tháng %02d / %04d  ▾",
                        thang,
                        nam
                )
        );
    }

    private void chonThang() {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.HORIZONTAL);

        int padding = (int) (16 * getResources()
                .getDisplayMetrics().density);

        layout.setPadding(padding, padding, padding, padding);

        NumberPicker chonThang = new NumberPicker(this);
        chonThang.setMinValue(1);
        chonThang.setMaxValue(12);
        chonThang.setValue(thang);
        chonThang.setWrapSelectorWheel(true);

        NumberPicker chonNam = new NumberPicker(this);
        chonNam.setMinValue(2000);
        chonNam.setMaxValue(2100);
        chonNam.setValue(nam);
        chonNam.setWrapSelectorWheel(false);

        layout.addView(
                chonThang,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        layout.addView(
                chonNam,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        new AlertDialog.Builder(this)
                .setTitle("Chọn tháng và năm")
                .setView(layout)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Chọn", (dialog, which) -> {

                    thang = chonThang.getValue();
                    nam = chonNam.getValue();

                    hienThiThang();
                    taiDuLieuNganSach();
                })
                .show();
    }

    private void taiDuLieuNganSach() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            thongBao("Vui lòng đăng nhập lại");
            return;
        }

        int lanTaiHienTai = ++lanTaiDuLieu;
        int thangCanTai = thang;
        int namCanTai = nam;

        Calendar batDau = Calendar.getInstance();
        batDau.clear();
        batDau.set(namCanTai, thangCanTai - 1, 1, 0, 0, 0);

        Calendar ketThuc = (Calendar) batDau.clone();
        ketThuc.add(Calendar.MONTH, 1);

        Date ngayBatDau = batDau.getTime();
        Date ngayKetThuc = ketThuc.getTime();

        String uid = nguoiDung.getUid();

        Task<QuerySnapshot> taskNganSach = firestore
                .collection("users")
                .document(uid)
                .collection("budgets")
                .get(Source.SERVER);

        Task<QuerySnapshot> taskGiaoDich = firestore
                .collection("users")
                .document(uid)
                .collection("transactions")
                .whereGreaterThanOrEqualTo(
                        "ngayGiaoDich", ngayBatDau
                )
                .whereLessThan(
                        "ngayGiaoDich", ngayKetThuc
                )
                .get(Source.SERVER);

        Tasks.whenAllSuccess(taskNganSach, taskGiaoDich)
                .addOnSuccessListener(ketQua -> {

                    if (lanTaiHienTai != lanTaiDuLieu) {
                        return;
                    }

                    try {

                        QuerySnapshot duLieuNganSach =
                                taskNganSach.getResult();

                        QuerySnapshot duLieuGiaoDich =
                                taskGiaoDich.getResult();

                        ArrayList<Budget> danhSachMoi =
                                new ArrayList<>();

                        Map<String, Long> chiTieuTheoDanhMuc =
                                new HashMap<>();

                        for (QueryDocumentSnapshot document
                                : duLieuGiaoDich) {

                            String loai =
                                    document.getString("loaiGiaoDich");

                            if (!"Chi tiêu".equals(loai)) {
                                continue;
                            }

                            String danhMuc =
                                    document.getString("danhMuc");

                            Long soTien =
                                    document.getLong("soTien");

                            if (danhMuc == null
                                    || soTien == null
                                    || soTien <= 0) {
                                continue;
                            }

                            long daChiCu =
                                    chiTieuTheoDanhMuc.getOrDefault(
                                            danhMuc, 0L
                                    );

                            chiTieuTheoDanhMuc.put(
                                    danhMuc,
                                    Math.addExact(daChiCu, soTien)
                            );
                        }

                        long tongNganSach = 0;
                        long tongDaChi = 0;

                        for (QueryDocumentSnapshot document
                                : duLieuNganSach) {

                            Long thangDuLieu =
                                    document.getLong("thang");

                            Long namDuLieu =
                                    document.getLong("nam");

                            if (thangDuLieu == null
                                    || namDuLieu == null) {
                                continue;
                            }

                            if (thangDuLieu != thangCanTai
                                    || namDuLieu != namCanTai) {
                                continue;
                            }

                            String danhMuc =
                                    document.getString("danhMuc");

                            Long gioiHan =
                                    document.getLong("soTienGioiHan");

                            if (danhMuc == null
                                    || gioiHan == null
                                    || gioiHan <= 0) {
                                continue;
                            }

                            long daChi =
                                    chiTieuTheoDanhMuc.getOrDefault(
                                            danhMuc, 0L
                                    );

                            Budget nganSach = new Budget(
                                    document.getId(),
                                    danhMuc,
                                    gioiHan,
                                    daChi,
                                    thangCanTai,
                                    namCanTai
                            );

                            danhSachMoi.add(nganSach);

                            tongNganSach = Math.addExact(
                                    tongNganSach, gioiHan
                            );

                            tongDaChi = Math.addExact(
                                    tongDaChi, daChi
                            );
                        }

                        danhSachMoi.sort(
                                (a, b) -> a.getDanhMuc()
                                        .compareToIgnoreCase(b.getDanhMuc())
                        );

                        danhSachNganSach.clear();
                        danhSachNganSach.addAll(danhSachMoi);

                        budgetAdapter.notifyDataSetChanged();

                        tvTongNganSach.setText(
                                dinhDangTien.format(tongNganSach)
                                        + " ₫"
                        );

                        tvDaChiNganSach.setText(
                                dinhDangTien.format(tongDaChi)
                                        + " ₫"
                        );

                        if (danhSachNganSach.isEmpty()) {

                            layoutNganSachTrong.setVisibility(
                                    View.VISIBLE
                            );

                            rvDanhSachNganSach.setVisibility(
                                    View.GONE
                            );

                        } else {

                            layoutNganSachTrong.setVisibility(
                                    View.GONE
                            );

                            rvDanhSachNganSach.setVisibility(
                                    View.VISIBLE
                            );
                        }

                    } catch (ArithmeticException e) {

                        thongBao(
                                "Tổng số tiền quá lớn, "
                                        + "không thể tính ngân sách"
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    if (lanTaiHienTai != lanTaiDuLieu) {
                        return;
                    }

                    thongBao(
                            "Không thể tải ngân sách: "
                                    + e.getMessage()
                    );
                });
    }

    private void thongBao(String noiDung) {
        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_LONG
        ).show();
    }

    private void hienThiQuanLyNganSach(Budget nganSach) {

        String[] luaChon = {
                "Chỉnh sửa hạn mức",
                "Xóa ngân sách"
        };

        new AlertDialog.Builder(this)
                .setTitle("Quản lý ngân sách " + nganSach.getDanhMuc())
                .setItems(luaChon, (dialog, which) -> {

                    if (which == 0) {
                        hienThiSuaNganSach(nganSach);
                    } else {
                        xacNhanXoaNganSach(nganSach);
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void hienThiSuaNganSach(Budget nganSach) {

        EditText edtGioiHan = new EditText(this);

        edtGioiHan.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtGioiHan.setSingleLine(true);

        edtGioiHan.setText(
                String.valueOf(nganSach.getSoTienGioiHan())
        );

        int padding = (int) (24 * getResources()
                .getDisplayMetrics().density);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, 12, padding, 0);
        layout.addView(edtGioiHan);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa ngân sách")
                .setMessage("Danh mục: " + nganSach.getDanhMuc())
                .setView(layout)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(v -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(view -> {

                        String giaTri = edtGioiHan.getText()
                                .toString().trim();

                        long gioiHanMoi;

                        try {
                            gioiHanMoi = Long.parseLong(giaTri);
                        } catch (NumberFormatException e) {
                            edtGioiHan.setError("Số tiền không hợp lệ");
                            return;
                        }

                        if (gioiHanMoi <= 0) {
                            edtGioiHan.setError(
                                    "Giới hạn phải lớn hơn 0"
                            );
                            return;
                        }

                        if (gioiHanMoi == nganSach.getSoTienGioiHan()) {
                            edtGioiHan.setError(
                                    "Giới hạn chưa thay đổi"
                            );
                            return;
                        }

                        FirebaseUser nguoiDung =
                                firebaseAuth.getCurrentUser();

                        if (nguoiDung == null) {
                            thongBao("Vui lòng đăng nhập lại");
                            return;
                        }

                        DocumentReference nganSachRef = firestore
                                .collection("users")
                                .document(nguoiDung.getUid())
                                .collection("budgets")
                                .document(nganSach.getMaNganSach());

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                .setEnabled(false);

                        firestore.runTransaction(transaction -> {

                            com.google.firebase.firestore.DocumentSnapshot doc =
                                    transaction.get(nganSachRef);

                            if (!doc.exists()) {
                                throw new IllegalStateException(
                                        "Ngân sách không tồn tại"
                                );
                            }

                            Long gioiHanHienTai =
                                    doc.getLong("soTienGioiHan");

                            if (gioiHanHienTai == null
                                    || gioiHanHienTai
                                    != nganSach.getSoTienGioiHan()) {
                                throw new IllegalStateException(
                                        "Ngân sách đã thay đổi, vui lòng tải lại"
                                );
                            }

                            transaction.update(
                                    nganSachRef,
                                    "soTienGioiHan", gioiHanMoi
                            );

                            return null;

                        }).addOnSuccessListener(unused -> {

                            dialog.dismiss();
                            thongBao("Cập nhật ngân sách thành công");
                            taiDuLieuNganSach();

                        }).addOnFailureListener(e -> {

                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setEnabled(true);

                            thongBao(
                                    "Không thể cập nhật: " + e.getMessage()
                            );
                        });
                    });
        });

        dialog.show();
    }

    private void xacNhanXoaNganSach(Budget nganSach) {

        new AlertDialog.Builder(this)
                .setTitle("Xóa ngân sách")
                .setMessage(
                        "Bạn muốn xóa ngân sách "
                                + nganSach.getDanhMuc()
                                + " tháng " + nganSach.getThang()
                                + "/" + nganSach.getNam()
                                + "?\n\n"
                                + "Các giao dịch Chi tiêu sẽ được giữ nguyên."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        xoaNganSach(nganSach))
                .show();
    }

    private void xoaNganSach(Budget nganSach) {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            thongBao("Vui lòng đăng nhập lại");
            return;
        }

        DocumentReference nganSachRef = firestore
                .collection("users")
                .document(nguoiDung.getUid())
                .collection("budgets")
                .document(nganSach.getMaNganSach());

        firestore.runTransaction(transaction -> {

            com.google.firebase.firestore.DocumentSnapshot doc =
                    transaction.get(nganSachRef);

            if (!doc.exists()) {
                throw new IllegalStateException(
                        "Ngân sách đã được xóa"
                );
            }

            Long gioiHanHienTai = doc.getLong("soTienGioiHan");

            if (gioiHanHienTai == null
                    || gioiHanHienTai != nganSach.getSoTienGioiHan()) {
                throw new IllegalStateException(
                        "Ngân sách đã thay đổi, vui lòng tải lại"
                );
            }

            transaction.delete(nganSachRef);

            return null;

        }).addOnSuccessListener(unused -> {

            thongBao("Đã xóa ngân sách");
            taiDuLieuNganSach();

        }).addOnFailureListener(e -> {

            thongBao("Không thể xóa: " + e.getMessage());
        });
    }


}
