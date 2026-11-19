package com.example.qlchitieu.data.repository;

import android.content.Context;

import com.example.qlchitieu.data.local.WalletDao;
import com.example.qlchitieu.model.Wallet;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity chỉ gọi lớp này, không gọi Firestore hay SQLite trực tiếp.
 *
 * Đây là bản CHUYỂN TIẾP: ghi SQLite trước, rồi đẩy luôn lên Firestore bằng cùng ID.
 * Khi viết xong SyncManager, phần đẩy lên cloud sẽ chuyển sang SyncManager.
 */
public class WalletRepository {

    public interface Callback {
        void onDone(boolean thanhCong, String loi);
    }

    private final WalletDao dao;
    private final FirebaseFirestore firestore;

    public WalletRepository(Context context) {
        dao = new WalletDao(context);
        firestore = FirebaseFirestore.getInstance();
    }

    private CollectionReference walletsOf(String userId) {
        return firestore.collection("users").document(userId).collection("wallets");
    }

    /** Đọc danh sách ví từ SQLite (nhanh, dùng được khi offline). */
    public List<Wallet> getWallets(String userId) {
        return dao.getAll(userId);
    }

    /** Thêm ví: lưu SQLite ngay, báo thành công ngay, đẩy lên cloud ở phía sau. */
    public void addWallet(String userId, String tenVi, String loaiVi,
                          long soDu, Callback callback) {

        // document().getId() sinh ID ngay trên máy, không cần mạng
        String maVi = walletsOf(userId).document().getId();

        Wallet vi = new Wallet(maVi, tenVi, loaiVi, soDu);
        vi.setTrangThai("HOAT_DONG");

        long now = System.currentTimeMillis();

        try {
            dao.insert(userId, vi, now);
        } catch (Exception e) {
            callback.onDone(false, e.getMessage());
            return;
        }

        callback.onDone(true, null);
        pushWallet(userId, vi, now);
    }

    private void pushWallet(String userId, Wallet vi, long ngayTao) {
        Map<String, Object> map = new HashMap<>();
        map.put("tenVi", vi.getTenVi());
        map.put("loaiVi", vi.getLoaiVi());
        map.put("soDu", vi.getSoDu());
        map.put("trangThai", vi.getTrangThai());
        map.put("ngayTao", new Timestamp(new Date(ngayTao)));
        map.put("ngayLuuTru", null);

        walletsOf(userId).document(vi.getMaVi())
                .set(map)
                .addOnSuccessListener(unused -> dao.markSynced(vi.getMaVi()));
        // Thất bại: ví vẫn ở trạng thái PENDING. Việc thử lại sẽ do SyncManager đảm nhiệm.
    }

    /**
     * TẠM THỜI: kéo ví từ Firestore về SQLite để các ví cũ (tạo trước khi có SQLite)
     * và các thay đổi từ màn hình chưa chuyển sang Repository được cập nhật.
     */
    public void pullFromCloud(String userId, Callback callback) {
        walletsOf(userId).get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {

                        String ten = doc.getString("tenVi");
                        String loai = doc.getString("loaiVi");
                        String trangThai = doc.getString("trangThai");
                        Long soDu = doc.getLong("soDu");

                        Wallet vi = new Wallet(
                                doc.getId(),
                                ten == null ? "Ví chưa đặt tên" : ten,
                                loai == null ? "Khác" : loai,
                                soDu == null ? 0L : soDu);
                        vi.setTrangThai(trangThai == null ? "HOAT_DONG" : trangThai);

                        Timestamp tao = doc.getTimestamp("ngayTao");
                        Timestamp luuTru = doc.getTimestamp("ngayLuuTru");

                        dao.upsertFromCloud(
                                userId, vi,
                                tao == null ? System.currentTimeMillis() : tao.toDate().getTime(),
                                luuTru == null ? 0 : luuTru.toDate().getTime());
                    }
                    callback.onDone(true, null);
                })
                .addOnFailureListener(e -> callback.onDone(false, e.getMessage()));
    }
}