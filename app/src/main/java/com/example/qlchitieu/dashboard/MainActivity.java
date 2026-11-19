package com.example.qlchitieu.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlchitieu.transaction.AddTransactionActivity;
import com.example.qlchitieu.budget.BudgetActivity;
import com.example.qlchitieu.R;
import com.example.qlchitieu.statistics.StatisticsActivity;
import com.example.qlchitieu.model.Transaction;
import com.example.qlchitieu.adapter.TransactionAdapter;
import com.example.qlchitieu.transaction.TransactionHistoryActivity;
import com.example.qlchitieu.wallet.WalletActivity;
import com.example.qlchitieu.auth.LoginActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView tvTenNguoiDung, tvTongSoDu, tvThuNhap, tvChiTieu;
    TextView navViTien, navGiaoDich;

    RecyclerView rvGiaoDichGanDay;
    LinearLayout layoutGiaoDichTrong;

    LinearLayout btnNganSach;
    LinearLayout layoutCanhBaoNganSach;

    TextView tvTieuDeCanhBaoNganSach;
    TextView tvNoiDungCanhBaoNganSach;
    TextView tvXemNganSach;

    ArrayList<Transaction> danhSachGiaoDich;
    TransactionAdapter transactionAdapter;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    int lanTaiCanhBao = 0;

    NumberFormat dinhDangTien =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            moDangNhap();
            return;
        }

        setContentView(R.layout.activity_main);

        firestore = FirebaseFirestore.getInstance();

        tvTenNguoiDung = findViewById(R.id.tvTenNguoiDung);
        tvTongSoDu = findViewById(R.id.tvTongSoDu);

        findViewById(R.id.cardTongSoDu).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    WalletActivity.class
            );
            startActivity(intent);
        });

        tvThuNhap = findViewById(R.id.tvThuNhap);
        tvChiTieu = findViewById(R.id.tvChiTieu);

        navViTien = findViewById(R.id.navViTien);
        navGiaoDich = findViewById(R.id.navGiaoDich);

        rvGiaoDichGanDay =
                findViewById(R.id.rvGiaoDichGanDay);

        layoutGiaoDichTrong =
                findViewById(R.id.layoutGiaoDichTrong);

        btnNganSach = findViewById(R.id.btnNganSach);

        layoutCanhBaoNganSach =
                findViewById(R.id.layoutCanhBaoNganSach);

        tvTieuDeCanhBaoNganSach =
                findViewById(R.id.tvTieuDeCanhBaoNganSach);

        tvNoiDungCanhBaoNganSach =
                findViewById(R.id.tvNoiDungCanhBaoNganSach);

        tvXemNganSach =
                findViewById(R.id.tvXemNganSach);

        TextView tvXemTatCa = findViewById(R.id.tvXemTatCa);
        TextView navThongKe = findViewById(R.id.navThongKe);

        danhSachGiaoDich = new ArrayList<>();

        transactionAdapter =
                new TransactionAdapter(danhSachGiaoDich);

        rvGiaoDichGanDay.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvGiaoDichGanDay.setNestedScrollingEnabled(false);
        rvGiaoDichGanDay.setAdapter(transactionAdapter);

        tvTongSoDu.setText("0 ₫");
        tvThuNhap.setText("0 ₫");
        tvChiTieu.setText("0 ₫");

        layoutCanhBaoNganSach.setVisibility(View.GONE);

        taiThongTinNguoiDung(nguoiDung.getUid());

        tvXemTatCa.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    TransactionHistoryActivity.class
            ));
        });

        btnNganSach.setOnClickListener(v -> moNganSach());

        tvXemNganSach.setOnClickListener(v -> moNganSach());

        layoutCanhBaoNganSach.setOnClickListener(
                v -> moNganSach()
        );

        navThongKe.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    StatisticsActivity.class
            ));
        });

        navViTien.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    WalletActivity.class
            ));
        });

        navGiaoDich.setOnClickListener(v -> {
            startActivity(new Intent(
                    this,
                    AddTransactionActivity.class
            ));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firestore != null) {
            taiTongSoDu();
            taiGiaoDichGanDay();
            taiCanhBaoNganSach();
            taiTongQuanNganSach();
        }
    }

    private void moNganSach() {
        Intent intent = new Intent(
                MainActivity.this,
                BudgetActivity.class
        );
        startActivity(intent);
    }

    private void taiThongTinNguoiDung(String uid) {

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {
                        String hoTen = document.getString("hoTen");

                        if (hoTen != null && !hoTen.trim().isEmpty()) {
                            tvTenNguoiDung.setText(hoTen);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    thongBao("Không thể tải thông tin người dùng");
                });
    }

    private void taiTongSoDu() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            return;
        }

        firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("wallets")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    long tongSoDu = 0;

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String trangThai =
                                document.getString("trangThai");

                        if ("LUU_TRU".equals(trangThai)) {
                            continue;
                        }

                        Long soDu = document.getLong("soDu");

                        if (soDu != null) {
                            tongSoDu += soDu;
                        }
                    }

                    tvTongSoDu.setText(
                            dinhDangTien.format(tongSoDu) + " ₫"
                    );
                })
                .addOnFailureListener(e -> {
                    thongBao("Không thể tải tổng số dư");
                });
    }

    private void taiGiaoDichGanDay() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            return;
        }

        firestore.collection("users")
                .document(nguoiDung.getUid())
                .collection("transactions")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    ArrayList<Transaction> tatCaGiaoDich =
                            new ArrayList<>();

                    long tongThuNhap = 0;
                    long tongChiTieu = 0;

                    Calendar homNay = Calendar.getInstance();

                    int thangHienTai = homNay.get(Calendar.MONTH);
                    int namHienTai = homNay.get(Calendar.YEAR);

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String maGiaoDich = document.getId();
                        String loai =
                                document.getString("loaiGiaoDich");

                        String danhMuc =
                                document.getString("danhMuc");

                        String ghiChu =
                                document.getString("ghiChu");

                        String maViNguon =
                                document.getString("maViNguon");

                        String maViDich =
                                document.getString("maViDich");

                        Long soTien = document.getLong("soTien");

                        Date ngayGiaoDich =
                                document.getDate("ngayGiaoDich");

                        if (soTien == null || ngayGiaoDich == null) {
                            continue;
                        }

                        Transaction giaoDich = new Transaction(
                                maGiaoDich,
                                loai,
                                danhMuc,
                                ghiChu,
                                maViNguon,
                                maViDich,
                                soTien,
                                ngayGiaoDich
                        );

                        tatCaGiaoDich.add(giaoDich);

                        Calendar lichGiaoDich =
                                Calendar.getInstance();

                        lichGiaoDich.setTime(ngayGiaoDich);

                        boolean cungThang =
                                lichGiaoDich.get(Calendar.MONTH)
                                        == thangHienTai
                                        && lichGiaoDich.get(Calendar.YEAR)
                                        == namHienTai;

                        if (cungThang) {

                            if ("Thu nhập".equals(loai)) {
                                tongThuNhap += soTien;

                            } else if ("Chi tiêu".equals(loai)) {
                                tongChiTieu += soTien;
                            }
                        }
                    }

                    tatCaGiaoDich.sort((gd1, gd2) ->
                            gd2.getNgayGiaoDich().compareTo(
                                    gd1.getNgayGiaoDich()
                            )
                    );

                    danhSachGiaoDich.clear();

                    int soLuongHienThi =
                            Math.min(5, tatCaGiaoDich.size());

                    for (int i = 0; i < soLuongHienThi; i++) {
                        danhSachGiaoDich.add(tatCaGiaoDich.get(i));
                    }

                    transactionAdapter.notifyDataSetChanged();

                    tvThuNhap.setText(
                            dinhDangTien.format(tongThuNhap) + " ₫"
                    );

                    tvChiTieu.setText(
                            dinhDangTien.format(tongChiTieu) + " ₫"
                    );

                    if (danhSachGiaoDich.isEmpty()) {

                        layoutGiaoDichTrong.setVisibility(
                                View.VISIBLE
                        );

                        rvGiaoDichGanDay.setVisibility(
                                View.GONE
                        );

                    } else {

                        layoutGiaoDichTrong.setVisibility(
                                View.GONE
                        );

                        rvGiaoDichGanDay.setVisibility(
                                View.VISIBLE
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    thongBao(
                            "Không thể tải lịch sử giao dịch: "
                                    + e.getMessage()
                    );
                });
    }

    private void taiCanhBaoNganSach() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            layoutCanhBaoNganSach.setVisibility(View.GONE);
            return;
        }

        int lanTaiHienTai = ++lanTaiCanhBao;

        layoutCanhBaoNganSach.setVisibility(View.GONE);

        Calendar batDau = Calendar.getInstance();
        batDau.set(Calendar.DAY_OF_MONTH, 1);
        batDau.set(Calendar.HOUR_OF_DAY, 0);
        batDau.set(Calendar.MINUTE, 0);
        batDau.set(Calendar.SECOND, 0);
        batDau.set(Calendar.MILLISECOND, 0);

        int thangHienTai = batDau.get(Calendar.MONTH) + 1;
        int namHienTai = batDau.get(Calendar.YEAR);

        Date ngayBatDau = batDau.getTime();

        Calendar ketThuc = (Calendar) batDau.clone();
        ketThuc.add(Calendar.MONTH, 1);

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

                    if (lanTaiHienTai != lanTaiCanhBao) {
                        return;
                    }

                    try {
                        Map<String, Long> chiTieuTheoDanhMuc =
                                new HashMap<>();

                        for (QueryDocumentSnapshot document
                                : taskGiaoDich.getResult()) {

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

                        ArrayList<String> canhBaoDo = new ArrayList<>();
                        ArrayList<String> canhBaoCam = new ArrayList<>();

                        for (QueryDocumentSnapshot document
                                : taskNganSach.getResult()) {

                            Long thang = document.getLong("thang");
                            Long nam = document.getLong("nam");

                            if (thang == null || nam == null
                                    || thang != thangHienTai
                                    || nam != namHienTai) {
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

                            double phanTram =
                                    (double) daChi * 100 / gioiHan;

                            if (phanTram >= 100) {

                                if (daChi > gioiHan) {

                                    long vuot =
                                            Math.subtractExact(
                                                    daChi, gioiHan
                                            );

                                    canhBaoDo.add(
                                            "• " + danhMuc
                                                    + ": Đã vượt "
                                                    + dinhDangTien.format(vuot)
                                                    + " ₫ ("
                                                    + dinhDangPhanTram(phanTram)
                                                    + ")."
                                    );

                                } else {

                                    canhBaoDo.add(
                                            "• " + danhMuc
                                                    + ": Đã đạt 100% ngân sách."
                                    );
                                }

                            } else if (phanTram >= 80) {

                                canhBaoCam.add(
                                        "• " + danhMuc
                                                + ": Đã sử dụng "
                                                + dinhDangPhanTram(phanTram)
                                                + " ngân sách."
                                );
                            }
                        }

                        hienThiCanhBao(
                                canhBaoDo,
                                canhBaoCam
                        );

                    } catch (ArithmeticException e) {

                        layoutCanhBaoNganSach.setVisibility(
                                View.GONE
                        );

                        thongBao("Dữ liệu chi tiêu vượt giới hạn tính toán");
                    }
                })
                .addOnFailureListener(e -> {

                    if (lanTaiHienTai != lanTaiCanhBao) {
                        return;
                    }

                    layoutCanhBaoNganSach.setVisibility(
                            View.GONE
                    );

                    thongBao(
                            "Không thể kiểm tra cảnh báo ngân sách: "
                                    + e.getMessage()
                    );
                });
    }

    private String dinhDangPhanTram(double phanTram) {

        return String.format(
                new Locale("vi", "VN"),
                "%.1f%%",
                phanTram
        );
    }

    private void hienThiCanhBao(
            ArrayList<String> canhBaoDo,
            ArrayList<String> canhBaoCam) {

        if (canhBaoDo.isEmpty() && canhBaoCam.isEmpty()) {

            layoutCanhBaoNganSach.setVisibility(View.GONE);
            return;
        }

        boolean coCanhBaoDo = !canhBaoDo.isEmpty();

        StringBuilder noiDung = new StringBuilder();

        for (String canhBao : canhBaoDo) {
            if (noiDung.length() > 0) {
                noiDung.append("\n");
            }
            noiDung.append(canhBao);
        }

        for (String canhBao : canhBaoCam) {
            if (noiDung.length() > 0) {
                noiDung.append("\n");
            }
            noiDung.append(canhBao);
        }

        tvNoiDungCanhBaoNganSach.setText(
                noiDung.toString()
        );

        if (coCanhBaoDo) {

            tvTieuDeCanhBaoNganSach.setText(
                    "⚠ Ngân sách đã đạt hoặc vượt giới hạn"
            );

            tvTieuDeCanhBaoNganSach.setTextColor(
                    Color.parseColor("#DC2626")
            );

        } else {

            tvTieuDeCanhBaoNganSach.setText(
                    "⚠ Ngân sách sắp đạt giới hạn"
            );

            tvTieuDeCanhBaoNganSach.setTextColor(
                    Color.parseColor("#D97706")
            );
        }

        layoutCanhBaoNganSach.setVisibility(View.VISIBLE);
    }

    private void moDangNhap() {

        Intent intent = new Intent(
                this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void thongBao(String noiDung) {
        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_LONG
        ).show();
    }

    private void taiTongQuanNganSach() {

        FirebaseUser nguoiDung = firebaseAuth.getCurrentUser();

        if (nguoiDung == null) {
            return;
        }

        TextView tvThang = findViewById(R.id.tvThangTongNganSach);
        TextView tvTien = findViewById(R.id.tvTienNganSach);
        TextView tvPhanTram = findViewById(R.id.tvPhanTramNganSach);
        TextView tvConLai = findViewById(R.id.tvConLaiNganSach);

        ProgressBar progress =
                findViewById(R.id.progressTongNganSach);

        Calendar batDau = Calendar.getInstance();

        batDau.set(Calendar.DAY_OF_MONTH, 1);
        batDau.set(Calendar.HOUR_OF_DAY, 0);
        batDau.set(Calendar.MINUTE, 0);
        batDau.set(Calendar.SECOND, 0);
        batDau.set(Calendar.MILLISECOND, 0);

        int thang = batDau.get(Calendar.MONTH) + 1;
        int nam = batDau.get(Calendar.YEAR);

        tvThang.setText(
                "Ngân sách tháng " + thang + "/" + nam
        );

        Date ngayBatDau = batDau.getTime();

        Calendar ketThuc = (Calendar) batDau.clone();
        ketThuc.add(Calendar.MONTH, 1);

        Date ngayKetThuc = ketThuc.getTime();

        String uid = nguoiDung.getUid();

        var taskNganSach = firestore
                .collection("users")
                .document(uid)
                .collection("budgets")
                .get(Source.SERVER);

        var taskGiaoDich = firestore
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

                    try {

                        Map<String, Long> chiTheoDanhMuc =
                                new HashMap<>();

                        for (QueryDocumentSnapshot document
                                : taskGiaoDich.getResult()) {

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
                                    chiTheoDanhMuc.getOrDefault(
                                            danhMuc, 0L
                                    );

                            chiTheoDanhMuc.put(
                                    danhMuc,
                                    Math.addExact(daChiCu, soTien)
                            );
                        }

                        long tongNganSach = 0;
                        long tongDaChi = 0;

                        for (QueryDocumentSnapshot document
                                : taskNganSach.getResult()) {

                            Long thangDuLieu =
                                    document.getLong("thang");

                            Long namDuLieu =
                                    document.getLong("nam");

                            Long gioiHan =
                                    document.getLong("soTienGioiHan");

                            String danhMuc =
                                    document.getString("danhMuc");

                            if (thangDuLieu == null
                                    || namDuLieu == null
                                    || gioiHan == null
                                    || danhMuc == null
                                    || gioiHan <= 0) {
                                continue;
                            }

                            if (thangDuLieu != thang
                                    || namDuLieu != nam) {
                                continue;
                            }

                            long daChi =
                                    chiTheoDanhMuc.getOrDefault(
                                            danhMuc, 0L
                                    );

                            tongNganSach = Math.addExact(
                                    tongNganSach, gioiHan
                            );

                            tongDaChi = Math.addExact(
                                    tongDaChi, daChi
                            );
                        }

                        if (tongNganSach == 0) {

                            tvTien.setText("Chưa có ngân sách tháng này");
                            tvPhanTram.setText("0%");
                            tvConLai.setText(
                                    "Nhấn để tạo ngân sách đầu tiên"
                            );

                            progress.setProgress(0);
                            return;
                        }

                        double phanTram =
                                (double) tongDaChi * 100 / tongNganSach;

                        int tienDo = (int) Math.min(
                                100,
                                Math.max(0, phanTram)
                        );

                        tvTien.setText(
                                "Đã chi "
                                        + dinhDangTien.format(tongDaChi)
                                        + " / "
                                        + dinhDangTien.format(tongNganSach)
                                        + " ₫"
                        );

                        tvPhanTram.setText(
                                String.format(
                                        Locale.forLanguageTag("vi-VN"),
                                        "%.0f%%",
                                        phanTram
                                )
                        );

                        progress.setProgress(tienDo);

                        long conLai = Math.subtractExact(
                                tongNganSach, tongDaChi
                        );

                        if (conLai >= 0) {

                            tvConLai.setText(
                                    "Còn lại: "
                                            + dinhDangTien.format(conLai)
                                            + " ₫"
                            );

                        } else {

                            tvConLai.setText(
                                    "Đã vượt tổng ngân sách "
                                            + dinhDangTien.format(
                                            Math.negateExact(conLai)
                                    )
                                            + " ₫"
                            );
                        }

                    } catch (ArithmeticException e) {

                        tvTien.setText(
                                "Số tiền vượt giới hạn tính toán"
                        );
                        tvPhanTram.setText("--");
                        tvConLai.setText("Không thể tính ngân sách");
                        progress.setProgress(0);
                    }

                })
                .addOnFailureListener(e -> {

                    tvTien.setText("Không thể tải ngân sách");
                    tvPhanTram.setText("--");
                    tvConLai.setText("Kiểm tra kết nối mạng");
                    progress.setProgress(0);
                });
    }

}
