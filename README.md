# gym_app
健身房系統建置:提升營運效率與客戶體驗之研究
---

！已知問題！：
(待處理)當沒有課程時，點擊物件ScheduledSet中的RecyclerView會閃退!

規劃方向：
短期:健身教練審核狀態顯示、個人資訊完善
中期:(參考:https://play.google.com/store/apps/details?id=com.jb.gms.admin&hl=zh_TW)
長期:串接Google Ads API, Google Authentication, Google Map, Google Calendar, 多國語言設定

專案開發日誌：
v0.1：連接SQL Server(侑宸)

v0.2：加入開啟程式動畫(侑宸)

v0.3：精簡「SQLConnection.class」準備結合啟動動畫、專案Gradle升級至8.6(侑宸)

(程式版本為「Android Studio Koala | 2024.1.1 June 13, 2024」)(侑宸)

v0.4：更改登入介面(侑宸)

v0.5：完成基礎登入功能(侑宸)

v0.6：完成註冊功能(侑宸)

v0.7：版面整理(侑宸)

v0.8：修復其它設備缺內建顏色問題(侑宸)

v0.9：完成切換教練與使用者分開登入(侑宸)

v0.10：完成切換教練與使用者分開註冊、修補各處(侑宸)

v0.11：加入使用者登入後廣告banner(宇哲)

v0.12：加入使用者介紹頁與教練介紹頁-待完成(耿豪)

v0.13：加入教練登入後廣告banner、修改登入後顯示name、登入後各功能按鈕(宇哲)

v0.14：對13版整理及修正(侑宸)

v0.15：教練/使用者登入頁的各功能按鈕導向(宇哲)

v0.16：新增使用者帳號檢視介面(侑宸)

v0.17：完成帳號檢視介面與圖像處理(侑宸)

v0.18：修復帳號檢視介面圖片更動與即時更新問題(侑宸)

v0.19：暫時可瀏覽資料庫教練 很多bug還需細修 篩選功能暫無(宇哲)

v0.20：加入初始介面提示、初步類別分類(侑宸)

v0.21：完成正常登出、自動登入帳號功能(侑宸)

v0.22：新增教練帳號檢視介面、補充初始介面細節(侑宸)

v0.23：準備處理查詢健身教練明細介面(侑宸)

v0.24：基礎切換查詢健身教練明細介面(侑宸)

v0.25：完成忘記帳號彈窗及發送郵件重設密碼功能(侑宸)

v0.26：完成教練與使用者廣告輪播與web同步，Advertisement.java為新增class(宇哲)

v0.27：使用SnackBar效果代替初始介面Toast提示、補充發送郵件失敗提示、準備處理重設密碼彈窗與驗證碼顯示效果(侑宸)

v0.28：新增多選日曆、時間選擇器安排課表、新增信箱登入方式、使用者首頁與健身教練首頁排版(侑宸)

v0.29：修復登入選項問題與教練列表問題(侑宸)

v0.30：新增返回箭頭圓形按鈕效果、文字格式、準備實作新增課程、課程列表與設定課程時間介面(侑宸)

v0.31：刪除暗黑模式影響、各處小問題修正、集中提示文字函式、完成新增課程與修改課程、準備實作安排班表(侑宸)

v0.32：完成安排班表與查看各日期班表、準備繼續補充各日期班表介面(侑宸)

v0.33：新增載入效果、登入介面修改(侑宸)

v0.34：補充載入效果、班表檢視介面只可選已排日期、準備實作班表詳細檢視與新增課表圖示簡化(侑宸)

v0.35：處理完新增安排班表版面、補充載入效果、套件名稱更改(侑宸)

v0.36：資料庫連線改為雲端、上傳至Github、實作使用者查詢介面、成功測試連接WebAPI(侑宸)

圖片：
R.drawable.main_login_ic_account：預設使用者圖片

補充：
1.extends AsyncTask<Void, Void, Void>為已棄用方法，請改用
【Executors.newSingleThreadExecutor().execute(() -> {
    //Background work here
    new Handler(Looper.getMainLooper()).post(() -> {
        //UI Thread work here
    });
});】方法
2.各個Activity中的ID請注意分別取不同名字，避免互相干擾，建議取名方式為「layout名稱_+元件功能+元件類型」，例：「forget_accountEdit」
當確認帳密無誤時可發送驗證碼，並將enable關掉，並隱藏帳密欄位顯示重設欄位
3.要使用google提供的javamail-android發送電子郵件首先需要Google帳戶啟用兩步驟驗證->取得應用程式密碼->使用此密碼登入JavaMailAPI
4.當遇到「It will always be more efficient to use more specific change events if you can. Rely on notifyDataSetChanged as a last resort.」問題，請將目前使用的RecycleView更新方法從notifyDataSetChanged()方法改成使用notifyItemRangeChanged(0, adList.size())