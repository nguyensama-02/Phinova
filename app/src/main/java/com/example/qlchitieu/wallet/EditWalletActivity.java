package com.example.qlchitieu.wallet;

import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditWalletActivity extends AppCompatActivity {

    EditText edtTenViSua;
    Spinner spLoaiViSua;
    TextView tvSoDuViSua, tvQuayLaiSuaVi;
    MaterialButton btnLuuSuaVi;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    String maVi;
    String[] danhSachLoaiVi = {
            "Tiền mặt",
            "Ngân hàng",
            "Ví điện tử"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_wallet);

        edtTenViSua = findViewById(R.id.edtTenViSua);
        spLoaiViSua = findViewById(R.id.spLoaiViSua);
        tvSoDuViSua = findViewById(R.id.tvSoDuViSua);
        tvQuayLaiSuaVi = findViewById(R.id.tvQuayLaiSuaVi);
        btnLuuSuaVi = findViewById(R.id.btnLuuSuaVi);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        maVi = getIntent().getStringExtra("maVi");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                danhSachLoaiVi
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spLoaiViSua.setAdapter(adapter);

        tvQuayLaiSuaVi.setOnClickListener(v -> finish());

        btnLuuSuaVi.setEnabled(false);
        btnLuuSuaVi.setOnClickListener(v -> luuThayDoi());

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

                    String tenVi = document.getString("tenVi");
                    String loaiVi = document.getString("loaiVi");
                    Long soDu = document.getLong("soDu");

                    edtTenViSua.setText(tenVi == null ? "" : tenVi);

                    for (int i = 0; i < danhSachLoaiVi.length; i++) {
                        if (danhSachLoaiVi[i].equals(loaiVi)) {
                            spLoaiViSua.setSelection(i);
                            break;
                        }
                    }

                    if (soDu == null) {
                        soDu = 0L;
                    }

                    NumberFormat dinhDang = NumberFormat.getInstance(
                            new Locale("vi", "VN")
                    );

                    tvSoDuViSua.setText(
                            dinhDang.format(soDu) + " ₫"
                    );

                    btnLuuSuaVi.setEnabled(true);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Không thể tải thông tin ví: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void luuThayDoi() {

        String tenViMoi = edtTenViSua.getText()
                .toString().trim();

        String loaiViMoi = spLoaiViSua.getSelectedItem()
                .toString();

        if (tenViMoi.isEmpty()) {
            edtTenViSua.setError("Vui lòng nhập tên ví");
            edtTenViSua.requestFocus();
            return;
        }

        if (tenViMoi.length() > 50) {
            edtTenViSua.setError("Tên ví tối đa 50 ký tự");
            return;
        }

        DocumentReference viRef = layViReference();

        if (viRef == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> duLieuMoi = new HashMap<>();
        duLieuMoi.put("tenVi", tenViMoi);
        duLieuMoi.put("loaiVi", loaiViMoi);

        btnLuuSuaVi.setEnabled(false);

        viRef.update(duLieuMoi)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this,
                            "Cập nhật ví thành công!",
                            Toast.LENGTH_SHORT).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    btnLuuSuaVi.setEnabled(true);

                    Toast.makeText(this,
                            "Không thể cập nhật ví: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
