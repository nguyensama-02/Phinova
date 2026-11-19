package com.example.qlchitieu.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Tạo và nâng cấp database SQLite của Phinova.
 * Quy ước chung cho mọi bảng:
 *  - Khóa chính là TEXT (dùng chính ID mà Firestore sinh ra) để SQLite và Firestore khớp nhau.
 *  - user_id: tách dữ liệu theo tài khoản (giống users/{uid} trên Firestore).
 *  - Ngày giờ lưu INTEGER = epoch mili-giây (Date.getTime()).
 *  - sync_status: PENDING (chưa lên cloud) / SYNCED (đã lên cloud).
 *  - is_deleted: xóa mềm (1 = đã xóa) để còn đồng bộ việc xóa lên cloud.
 */
public class PhinovaDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "phinova.db";
    private static final int DB_VERSION = 1;

    // ----- Tên bảng -----
    public static final String T_WALLETS = "wallets";
    public static final String T_TRANSACTIONS = "transactions";
    public static final String T_BUDGETS = "budgets";
    public static final String T_ADJUSTMENTS = "balance_adjustments";

    // ----- Cột dùng chung -----
    public static final String C_USER_ID = "user_id";
    public static final String C_SYNC_STATUS = "sync_status";
    public static final String C_UPDATED_AT = "updated_at";
    public static final String C_IS_DELETED = "is_deleted";

    public static final String SYNC_PENDING = "PENDING";
    public static final String SYNC_SYNCED = "SYNCED";

    // ----- wallets -----
    public static final String W_ID = "ma_vi";
    public static final String W_TEN = "ten_vi";
    public static final String W_LOAI = "loai_vi";
    public static final String W_SO_DU = "so_du";
    public static final String W_TRANG_THAI = "trang_thai";
    public static final String W_NGAY_TAO = "ngay_tao";
    public static final String W_NGAY_LUU_TRU = "ngay_luu_tru";

    // ----- transactions -----
    public static final String TX_ID = "ma_giao_dich";
    public static final String TX_LOAI = "loai_giao_dich";
    public static final String TX_DANH_MUC = "danh_muc";
    public static final String TX_GHI_CHU = "ghi_chu";
    public static final String TX_VI_NGUON = "ma_vi_nguon";
    public static final String TX_VI_DICH = "ma_vi_dich";
    public static final String TX_SO_TIEN = "so_tien";
    public static final String TX_NGAY = "ngay_giao_dich";

    // ----- budgets (không lưu so_tien_da_chi: tính bằng SUM từ transactions) -----
    public static final String B_ID = "ma_ngan_sach";
    public static final String B_DANH_MUC = "danh_muc";
    public static final String B_GIOI_HAN = "so_tien_gioi_han";
    public static final String B_THANG = "thang";
    public static final String B_NAM = "nam";

    // ----- balance_adjustments -----
    public static final String A_ID = "ma_dieu_chinh";
    public static final String A_VI = "ma_vi";
    public static final String A_LY_DO = "ly_do";
    public static final String A_SO_DU_CU = "so_du_cu";
    public static final String A_SO_DU_MOI = "so_du_moi";
    public static final String A_CHENH_LECH = "chenh_lech";
    public static final String A_NGAY = "ngay_dieu_chinh";

    // Cột đồng bộ, chèn vào cuối mỗi bảng
    private static final String SYNC_COLUMNS =
            C_SYNC_STATUS + " TEXT NOT NULL DEFAULT 'PENDING', " +
                    C_UPDATED_AT + " INTEGER NOT NULL DEFAULT 0, " +
                    C_IS_DELETED + " INTEGER NOT NULL DEFAULT 0";

    private static PhinovaDbHelper instance;

    /** Cả app dùng chung một kết nối database. */
    public static synchronized PhinovaDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PhinovaDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private PhinovaDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_WALLETS + " (" +
                W_ID + " TEXT PRIMARY KEY, " +
                C_USER_ID + " TEXT NOT NULL, " +
                W_TEN + " TEXT NOT NULL, " +
                W_LOAI + " TEXT, " +
                W_SO_DU + " INTEGER NOT NULL DEFAULT 0, " +
                W_TRANG_THAI + " TEXT NOT NULL DEFAULT 'HOAT_DONG', " +
                W_NGAY_TAO + " INTEGER, " +
                W_NGAY_LUU_TRU + " INTEGER, " +
                SYNC_COLUMNS + ")");

        db.execSQL("CREATE TABLE " + T_TRANSACTIONS + " (" +
                TX_ID + " TEXT PRIMARY KEY, " +
                C_USER_ID + " TEXT NOT NULL, " +
                TX_LOAI + " TEXT NOT NULL, " +
                TX_DANH_MUC + " TEXT, " +
                TX_GHI_CHU + " TEXT, " +
                TX_VI_NGUON + " TEXT, " +
                TX_VI_DICH + " TEXT, " +
                TX_SO_TIEN + " INTEGER NOT NULL, " +
                TX_NGAY + " INTEGER NOT NULL, " +
                SYNC_COLUMNS + ")");

        db.execSQL("CREATE TABLE " + T_BUDGETS + " (" +
                B_ID + " TEXT PRIMARY KEY, " +
                C_USER_ID + " TEXT NOT NULL, " +
                B_DANH_MUC + " TEXT NOT NULL, " +
                B_GIOI_HAN + " INTEGER NOT NULL, " +
                B_THANG + " INTEGER NOT NULL, " +
                B_NAM + " INTEGER NOT NULL, " +
                SYNC_COLUMNS + ")");

        db.execSQL("CREATE TABLE " + T_ADJUSTMENTS + " (" +
                A_ID + " TEXT PRIMARY KEY, " +
                C_USER_ID + " TEXT NOT NULL, " +
                A_VI + " TEXT NOT NULL, " +
                A_LY_DO + " TEXT, " +
                A_SO_DU_CU + " INTEGER NOT NULL, " +
                A_SO_DU_MOI + " INTEGER NOT NULL, " +
                A_CHENH_LECH + " INTEGER NOT NULL, " +
                A_NGAY + " INTEGER NOT NULL, " +
                SYNC_COLUMNS + ")");

        // Index cho các truy vấn thường dùng
        db.execSQL("CREATE INDEX idx_wallets_user ON " + T_WALLETS + "(" + C_USER_ID + ")");
        db.execSQL("CREATE INDEX idx_tx_user_date ON " + T_TRANSACTIONS +
                "(" + C_USER_ID + ", " + TX_NGAY + ")");
        db.execSQL("CREATE INDEX idx_budget_user_month ON " + T_BUDGETS +
                "(" + C_USER_ID + ", " + B_NAM + ", " + B_THANG + ")");
        db.execSQL("CREATE INDEX idx_adj_wallet ON " + T_ADJUSTMENTS +
                "(" + A_VI + ", " + A_NGAY + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // CHỈ dùng khi đang phát triển: xóa và tạo lại.
        // Sau khi có dữ liệu thật, đổi sang ALTER TABLE theo từng phiên bản.
        db.execSQL("DROP TABLE IF EXISTS " + T_ADJUSTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + T_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + T_WALLETS);
        onCreate(db);
    }
}