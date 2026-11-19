package com.example.qlchitieu.transaction;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlchitieu.R;
import com.example.qlchitieu.adapter.TransactionAdapter;
import com.example.qlchitieu.model.Transaction;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TransactionHistoryActivity extends AppCompatActivity {

    EditText edtTimGiaoDich;
    TextView tvQuayLaiLichSu, tvSoLuongGiaoDich;
    MaterialButtonToggleGroup toggleLocGiaoDich;

    RecyclerView rvLichSuGiaoDich;
    LinearLayout layoutLichSuTrong;

    ArrayList<Transaction> tatCaGiaoDich;
    ArrayList<Transaction> danhSachHienThi;

    TransactionAdapter transactionAdapter;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        edtTimGiaoDich = findViewById(R.id.edtTimGiaoDich);
        tvQuayLaiLichSu = findViewById(R.id.tvQuayLaiLichSu);
        tvSoLuongGiaoDich = findViewById(R.id.tvSoLuongGiaoDich);

        toggleLocGiaoDich = findViewById(R.id.toggleLocGiaoDich);

        rvLichSuGiaoDich = findViewById(R.id.rvLichSuGiaoDich);
        layoutLichSuTrong = findViewById(R.id.layoutLichSuTrong);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        tatCaGiaoDich = new ArrayList<>();
        danhSachHienThi = new ArrayList<>();

        transactionAdapter = new TransactionAdapter(danhSachHienThi);

        rvLichSuGiaoDich.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvLichSuGiaoDich.setAdapter(transactionAdapter);

        tvQuayLaiLichSu.setOnClickListener(v -> finish());

        toggleLocGiaoDich.addOnButtonCheckedListener(
                (group, checkedId, isChecked) -> {
                    if (isChecked) {
                        locGiaoDich();
                    }
                }
        );

        edtTimGiaoDich.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                locGiaoDich();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        taiLichSuGiaoDich();
    }

    private void taiLichSuGiaoDich() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("transactions")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    tatCaGiaoDich.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        Long soTien = document.getLong("soTien");
                        Date ngayGiaoDich =
                                document.getDate("ngayGiaoDich");

                        if (soTien == null || ngayGiaoDich == null) {
                            continue;
                        }

                        Transaction giaoDich = new Transaction(
                                document.getId(),
                                document.getString("loaiGiaoDich"),
                                document.getString("danhMuc"),
                                document.getString("ghiChu"),
                                document.getString("maViNguon"),
                                document.getString("maViDich"),
                                soTien,
                                ngayGiaoDich
                        );

                        tatCaGiaoDich.add(giaoDich);
                    }

                    tatCaGiaoDich.sort((gd1, gd2) ->
                            gd2.getNgayGiaoDich().compareTo(
                                    gd1.getNgayGiaoDich()
                            )
                    );

                    locGiaoDich();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Không thể tải lịch sử: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void locGiaoDich() {

        String tuKhoa = edtTimGiaoDich.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        int nutDuocChon = toggleLocGiaoDich.getCheckedButtonId();

        danhSachHienThi.clear();

        for (Transaction giaoDich : tatCaGiaoDich) {

            String loai = giaoDich.getLoaiGiaoDich();
            String danhMuc = giaoDich.getDanhMuc();
            String ghiChu = giaoDich.getGhiChu();

            if (loai == null) {
                loai = "";
            }

            if (danhMuc == null) {
                danhMuc = "";
            }

            if (ghiChu == null) {
                ghiChu = "";
            }

            boolean dungLoai = true;

            if (nutDuocChon == R.id.btnLocThu) {
                dungLoai = loai.equals("Thu nhập");
            } else if (nutDuocChon == R.id.btnLocChi) {
                dungLoai = loai.equals("Chi tiêu");
            } else if (nutDuocChon == R.id.btnLocChuyen) {
                dungLoai = loai.equals("Chuyển tiền");
            }

            boolean dungTuKhoa =
                    danhMuc.toLowerCase(Locale.ROOT).contains(tuKhoa)
                            || ghiChu.toLowerCase(Locale.ROOT).contains(tuKhoa);

            if (dungLoai && dungTuKhoa) {
                danhSachHienThi.add(giaoDich);
            }
        }

        transactionAdapter.notifyDataSetChanged();

        tvSoLuongGiaoDich.setText(
                danhSachHienThi.size() + " giao dịch"
        );

        if (danhSachHienThi.isEmpty()) {
            layoutLichSuTrong.setVisibility(View.VISIBLE);
            rvLichSuGiaoDich.setVisibility(View.GONE);
        } else {
            layoutLichSuTrong.setVisibility(View.GONE);
            rvLichSuGiaoDich.setVisibility(View.VISIBLE);
        }
    }
}
