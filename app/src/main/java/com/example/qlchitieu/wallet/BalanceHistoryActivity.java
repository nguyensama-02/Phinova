package com.example.qlchitieu.wallet;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlchitieu.R;
import com.example.qlchitieu.adapter.BalanceAdjustmentAdapter;
import com.example.qlchitieu.model.BalanceAdjustment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;

public class BalanceHistoryActivity extends AppCompatActivity {

    TextView tvQuayLaiLichSuDieuChinh;
    TextView tvTenViLichSu, tvSoLanDieuChinh;

    LinearLayout layoutLichSuDieuChinhTrong;
    RecyclerView rvLichSuDieuChinh;

    ArrayList<BalanceAdjustment> danhSachDieuChinh;
    BalanceAdjustmentAdapter adapter;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    String maVi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_history);

        tvQuayLaiLichSuDieuChinh =
                findViewById(R.id.tvQuayLaiLichSuDieuChinh);

        tvTenViLichSu = findViewById(R.id.tvTenViLichSu);
        tvSoLanDieuChinh = findViewById(R.id.tvSoLanDieuChinh);

        layoutLichSuDieuChinhTrong =
                findViewById(R.id.layoutLichSuDieuChinhTrong);

        rvLichSuDieuChinh =
                findViewById(R.id.rvLichSuDieuChinh);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        maVi = getIntent().getStringExtra("maVi");

        danhSachDieuChinh = new ArrayList<>();

        adapter = new BalanceAdjustmentAdapter(danhSachDieuChinh);

        rvLichSuDieuChinh.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvLichSuDieuChinh.setAdapter(adapter);

        tvQuayLaiLichSuDieuChinh.setOnClickListener(v -> finish());

        if (maVi == null || maVi.trim().isEmpty()) {
            Toast.makeText(this,
                    "Không tìm thấy mã ví",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            Toast.makeText(this,
                    "Vui lòng đăng nhập lại",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        taiTenVi();
        taiLichSuDieuChinh();
    }

    private void taiTenVi() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            return;
        }

        DocumentReference viRef = firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("wallets")
                .document(maVi);

        viRef.get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        String tenVi = document.getString("tenVi");

                        tvTenViLichSu.setText(
                                tenVi == null ? "Ví tiền" : tenVi
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Không thể tải tên ví",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void taiLichSuDieuChinh() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            return;
        }

        firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("balance_adjustments")
                .whereEqualTo("maVi", maVi)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    danhSachDieuChinh.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        Long soDuCu = document.getLong("soDuCu");
                        Long soDuMoi = document.getLong("soDuMoi");
                        Long chenhLech = document.getLong("chenhLech");

                        if (soDuCu == null
                                || soDuMoi == null
                                || chenhLech == null) {
                            continue;
                        }

                        String lyDo = document.getString("lyDo");
                        Date ngayDieuChinh =
                                document.getDate("ngayDieuChinh");

                        BalanceAdjustment dieuChinh =
                                new BalanceAdjustment(
                                        document.getId(),
                                        document.getString("maVi"),
                                        lyDo,
                                        soDuCu,
                                        soDuMoi,
                                        chenhLech,
                                        ngayDieuChinh
                                );

                        danhSachDieuChinh.add(dieuChinh);
                    }

                    danhSachDieuChinh.sort((a, b) -> {

                        Date ngayA = a.getNgayDieuChinh();
                        Date ngayB = b.getNgayDieuChinh();

                        if (ngayA == null && ngayB == null) {
                            return 0;
                        }

                        if (ngayA == null) {
                            return 1;
                        }

                        if (ngayB == null) {
                            return -1;
                        }

                        return ngayB.compareTo(ngayA);
                    });

                    adapter.notifyDataSetChanged();

                    tvSoLanDieuChinh.setText(
                            danhSachDieuChinh.size() + " lần điều chỉnh"
                    );

                    if (danhSachDieuChinh.isEmpty()) {

                        layoutLichSuDieuChinhTrong.setVisibility(
                                View.VISIBLE
                        );

                        rvLichSuDieuChinh.setVisibility(
                                View.GONE
                        );

                    } else {

                        layoutLichSuDieuChinhTrong.setVisibility(
                                View.GONE
                        );

                        rvLichSuDieuChinh.setVisibility(
                                View.VISIBLE
                        );
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Không thể tải lịch sử: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
