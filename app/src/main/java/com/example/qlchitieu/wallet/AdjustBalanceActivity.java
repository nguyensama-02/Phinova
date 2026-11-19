package com.example.qlchitieu.wallet;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlchitieu.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdjustBalanceActivity extends AppCompatActivity {

    TextView tvQuayLaiDieuChinh, tvSoDuHienTai;
    TextView tvTenViDieuChinh, tvChenhLechDieuChinh;

    EditText edtSoDuThucTe, edtLyDoDieuChinh;
    MaterialButton btnXacNhanDieuChinh;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    String maVi;
    long soDuHienTai = 0;
    boolean daTaiVi = false;

    NumberFormat dinhDangTien =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_balance);

        tvQuayLaiDieuChinh = findViewById(R.id.tvQuayLaiDieuChinh);
        tvSoDuHienTai = findViewById(R.id.tvSoDuHienTai);
        tvTenViDieuChinh = findViewById(R.id.tvTenViDieuChinh);
        tvChenhLechDieuChinh = findViewById(R.id.tvChenhLechDieuChinh);

        edtSoDuThucTe = findViewById(R.id.edtSoDuThucTe);
        edtLyDoDieuChinh = findViewById(R.id.edtLyDoDieuChinh);
        btnXacNhanDieuChinh = findViewById(R.id.btnXacNhanDieuChinh);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        maVi = getIntent().getStringExtra("maVi");

        tvQuayLaiDieuChinh.setOnClickListener(v -> finish());

        btnXacNhanDieuChinh.setEnabled(false);

        edtSoDuThucTe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                tinhChenhLech();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnXacNhanDieuChinh.setOnClickListener(v -> xacNhanDieuChinh());

        if (maVi == null || maVi.trim().isEmpty()) {
            Toast.makeText(this,
                    "Không tìm thấy mã ví",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        taiThongTinVi();
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

    private void taiThongTinVi() {

        DocumentReference viRef = layViReference();

        if (viRef == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viRef.get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        Toast.makeText(this,
                                "Ví không tồn tại",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    Long soDu = document.getLong("soDu");

                    if (soDu == null) {
                        Toast.makeText(this,
                                "Số dư ví không hợp lệ",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    soDuHienTai = soDu;
                    daTaiVi = true;

                    String tenVi = document.getString("tenVi");

                    tvTenViDieuChinh.setText(
                            tenVi == null ? "Ví tiền" : tenVi
                    );

                    tvSoDuHienTai.setText(
                            dinhDangTien.format(soDuHienTai) + " ₫"
                    );

                    btnXacNhanDieuChinh.setEnabled(true);
                    tinhChenhLech();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Không thể tải ví: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void tinhChenhLech() {

        String soDuNhap = edtSoDuThucTe.getText()
                .toString().trim();

        if (!daTaiVi || soDuNhap.isEmpty()) {
            tvChenhLechDieuChinh.setText("0 ₫");
            tvChenhLechDieuChinh.setTextColor(
                    Color.parseColor("#102A4C")
            );
            return;
        }

        try {
            long soDuMoi = Long.parseLong(soDuNhap);
            long chenhLech = Math.subtractExact(
                    soDuMoi,
                    soDuHienTai
            );

            String dau = chenhLech > 0 ? "+" : "";

            tvChenhLechDieuChinh.setText(
                    dau + dinhDangTien.format(chenhLech) + " ₫"
            );

            if (chenhLech > 0) {
                tvChenhLechDieuChinh.setTextColor(
                        Color.parseColor("#059669")
                );
            } else if (chenhLech < 0) {
                tvChenhLechDieuChinh.setTextColor(
                        Color.parseColor("#DC2626")
                );
            } else {
                tvChenhLechDieuChinh.setTextColor(
                        Color.parseColor("#102A4C")
                );
            }

        } catch (NumberFormatException | ArithmeticException e) {

            tvChenhLechDieuChinh.setText("Số tiền không hợp lệ");
            tvChenhLechDieuChinh.setTextColor(
                    Color.parseColor("#DC2626")
            );
        }
    }

    private void xacNhanDieuChinh() {

        if (!daTaiVi) {
            return;
        }

        String soDuNhap = edtSoDuThucTe.getText()
                .toString().trim();

        String lyDo = edtLyDoDieuChinh.getText()
                .toString().trim();

        if (soDuNhap.isEmpty()) {
            edtSoDuThucTe.setError("Vui lòng nhập số dư thực tế");
            return;
        }

        long soDuMoi;

        try {
            soDuMoi = Long.parseLong(soDuNhap);
        } catch (NumberFormatException e) {
            edtSoDuThucTe.setError("Số tiền không hợp lệ");
            return;
        }

        if (soDuMoi < 0) {
            edtSoDuThucTe.setError("Số dư không được âm");
            return;
        }

        if (lyDo.isEmpty()) {
            edtLyDoDieuChinh.setError("Vui lòng nhập lý do");
            return;
        }

        if (lyDo.length() > 500) {
            edtLyDoDieuChinh.setError("Lý do tối đa 500 ký tự");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận điều chỉnh")
                .setMessage(
                        "Số dư thực tế: "
                                + dinhDangTien.format(soDuMoi)
                                + " ₫\n\n"
                                + "Lý do: " + lyDo
                                + "\n\nSố dư sẽ được kiểm tra lại trước khi lưu."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (dialog, which) ->
                        luuDieuChinh(soDuMoi, lyDo))
                .show();
    }

    private void luuDieuChinh(long soDuMoi, String lyDo) {

        DocumentReference viRef = layViReference();

        if (viRef == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        DocumentReference lichSuRef = firestore
                .collection("users")
                .document(nguoiDung.getUid())
                .collection("balance_adjustments")
                .document();

        btnXacNhanDieuChinh.setEnabled(false);

        firestore.runTransaction(transaction -> {

            DocumentSnapshot document = transaction.get(viRef);

            if (!document.exists()) {
                throw new IllegalStateException("Ví không tồn tại");
            }

            Long soDuCu = document.getLong("soDu");

            if (soDuCu == null) {
                throw new IllegalStateException("Số dư ví không hợp lệ");
            }

            long chenhLech = Math.subtractExact(soDuMoi, soDuCu);

            if (chenhLech == 0) {
                throw new IllegalStateException(
                        "Số dư mới không thay đổi"
                );
            }

            Map<String, Object> lichSu = new HashMap<>();

            lichSu.put("maVi", maVi);
            lichSu.put("soDuCu", soDuCu);
            lichSu.put("soDuMoi", soDuMoi);
            lichSu.put("chenhLech", chenhLech);
            lichSu.put("lyDo", lyDo);
            lichSu.put("ngayDieuChinh", FieldValue.serverTimestamp());

            transaction.update(viRef, "soDu", soDuMoi);
            transaction.set(lichSuRef, lichSu);

            return null;

        }).addOnSuccessListener(unused -> {

            Toast.makeText(this,
                    "Điều chỉnh số dư thành công!",
                    Toast.LENGTH_SHORT).show();

            finish();

        }).addOnFailureListener(e -> {

            btnXacNhanDieuChinh.setEnabled(true);

            Toast.makeText(this,
                    "Không thể điều chỉnh: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        });
    }
}
