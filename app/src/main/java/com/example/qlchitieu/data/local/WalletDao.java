package com.example.qlchitieu.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.qlchitieu.model.Wallet;

import java.util.ArrayList;
import java.util.List;

/**
 * Chỉ làm việc với SQLite cho bảng wallets. Không biết gì về Firestore.
 */
public class WalletDao {

    private final PhinovaDbHelper helper;

    public WalletDao(Context context) {
        helper = PhinovaDbHelper.getInstance(context);
    }

    /** Thêm ví mới tạo trên máy: đánh dấu PENDING (chưa lên cloud). */
    public void insert(String userId, Wallet w, long now) {
        ContentValues v = new ContentValues();
        v.put(PhinovaDbHelper.W_ID, w.getMaVi());
        v.put(PhinovaDbHelper.C_USER_ID, userId);
        v.put(PhinovaDbHelper.W_TEN, w.getTenVi());
        v.put(PhinovaDbHelper.W_LOAI, w.getLoaiVi());
        v.put(PhinovaDbHelper.W_SO_DU, w.getSoDu());
        v.put(PhinovaDbHelper.W_TRANG_THAI, w.getTrangThai());
        v.put(PhinovaDbHelper.W_NGAY_TAO, now);
        v.put(PhinovaDbHelper.C_SYNC_STATUS, PhinovaDbHelper.SYNC_PENDING);
        v.put(PhinovaDbHelper.C_UPDATED_AT, now);
        v.put(PhinovaDbHelper.C_IS_DELETED, 0);

        helper.getWritableDatabase().insertOrThrow(PhinovaDbHelper.T_WALLETS, null, v);
    }

    /** Lấy tất cả ví chưa bị xóa của một người dùng (cả hoạt động và lưu trữ). */
    public List<Wallet> getAll(String userId) {
        List<Wallet> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor c = db.query(
                PhinovaDbHelper.T_WALLETS,
                null,
                PhinovaDbHelper.C_USER_ID + "=? AND " + PhinovaDbHelper.C_IS_DELETED + "=0",
                new String[]{userId},
                null, null,
                PhinovaDbHelper.W_NGAY_TAO + " ASC")) {

            while (c.moveToNext()) {
                Wallet w = new Wallet(
                        c.getString(c.getColumnIndexOrThrow(PhinovaDbHelper.W_ID)),
                        c.getString(c.getColumnIndexOrThrow(PhinovaDbHelper.W_TEN)),
                        c.getString(c.getColumnIndexOrThrow(PhinovaDbHelper.W_LOAI)),
                        c.getLong(c.getColumnIndexOrThrow(PhinovaDbHelper.W_SO_DU)));
                w.setTrangThai(c.getString(c.getColumnIndexOrThrow(PhinovaDbHelper.W_TRANG_THAI)));
                list.add(w);
            }
        }
        return list;
    }

    /** Ví đã đẩy lên cloud thành công. */
    public void markSynced(String maVi) {
        ContentValues v = new ContentValues();
        v.put(PhinovaDbHelper.C_SYNC_STATUS, PhinovaDbHelper.SYNC_SYNCED);
        helper.getWritableDatabase().update(
                PhinovaDbHelper.T_WALLETS, v,
                PhinovaDbHelper.W_ID + "=?", new String[]{maVi});
    }

    /**
     * Ghi dữ liệu kéo từ cloud về. Nếu ví này đang có thay đổi chưa đẩy lên (PENDING)
     * thì bỏ qua để không ghi đè mất dữ liệu trên máy.
     */
    public void upsertFromCloud(String userId, Wallet w, long ngayTao, long ngayLuuTru) {
        SQLiteDatabase db = helper.getWritableDatabase();

        try (Cursor c = db.query(
                PhinovaDbHelper.T_WALLETS,
                new String[]{PhinovaDbHelper.C_SYNC_STATUS},
                PhinovaDbHelper.W_ID + "=?", new String[]{w.getMaVi()},
                null, null, null)) {

            if (c.moveToFirst() && PhinovaDbHelper.SYNC_PENDING.equals(c.getString(0))) {
                return;
            }
        }

        ContentValues v = new ContentValues();
        v.put(PhinovaDbHelper.W_ID, w.getMaVi());
        v.put(PhinovaDbHelper.C_USER_ID, userId);
        v.put(PhinovaDbHelper.W_TEN, w.getTenVi());
        v.put(PhinovaDbHelper.W_LOAI, w.getLoaiVi());
        v.put(PhinovaDbHelper.W_SO_DU, w.getSoDu());
        v.put(PhinovaDbHelper.W_TRANG_THAI, w.getTrangThai());
        v.put(PhinovaDbHelper.W_NGAY_TAO, ngayTao);
        if (ngayLuuTru > 0) {
            v.put(PhinovaDbHelper.W_NGAY_LUU_TRU, ngayLuuTru);
        }
        v.put(PhinovaDbHelper.C_SYNC_STATUS, PhinovaDbHelper.SYNC_SYNCED);
        v.put(PhinovaDbHelper.C_UPDATED_AT, System.currentTimeMillis());
        v.put(PhinovaDbHelper.C_IS_DELETED, 0);

        db.insertWithOnConflict(PhinovaDbHelper.T_WALLETS, null, v,
                SQLiteDatabase.CONFLICT_REPLACE);
    }
}