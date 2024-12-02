# gym_app

健身房系統建置:提升營運效率與客戶體驗之研究
---

- [gym_app](#gym_app)
    * [健身房系統建置:提升營運效率與客戶體驗之研究](#健身房系統建置提升營運效率與客戶體驗之研究)
    * [規劃方向：](#規劃方向)
    * [專案開發日誌：](#專案開發日誌)
    * [補充：](#補充)
        + [圖片：](#圖片)
        + [Android Studio 操作](#android-studio-操作)
        + [Android Studio 程式](#android-studio-程式)
        + [Github 部分](#github-部分)
        + [補充參考資料](#補充參考資料)

！已知問題！：

(待處理)當沒有課程時，點擊物件ScheduledSet中的RecyclerView會閃退!

## 規劃方向：

[參考](https://play.google.com/store/apps/details?id=com.jb.gms.admin&hl=zh_TW)

## 專案開發日誌：

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

v0.27：使用SnackBar效果代替初始介面Toast提示、補充發送郵件失敗提示、
準備處理重設密碼彈窗與驗證碼顯示效果(侑宸)

v0.28：新增多選日曆、時間選擇器安排課表、新增信箱登入方式、使用者首頁與健身教練首頁排版(侑宸)

v0.29：修復登入選項問題與教練列表問題(侑宸)

v0.30：新增返回箭頭圓形按鈕效果、文字格式、準備實作新增課程、課程列表與設定課程時間介面(侑宸)

v0.31：刪除暗黑模式影響、各處小問題修正、集中提示文字函式、完成新增課程與修改課程、準備實作安排班表(侑宸)

v0.32：完成安排班表與查看各日期班表、準備繼續補充各日期班表介面(侑宸)

v0.33：新增載入效果、登入介面修改(侑宸)

v0.34：補充載入效果、班表檢視介面只可選已排日期、準備實作班表詳細檢視與新增課表圖示簡化(侑宸)

v0.35：處理完新增安排班表版面、補充載入效果、套件名稱更改(侑宸)

v0.36：資料庫連線改為雲端、上傳至Github、實作使用者查詢介面、成功測試連接WebAPI(侑宸)

v0.37：整合日曆搜尋、教練搜尋、課程搜尋至搜尋介面，並以分頁形式切換，準備實作預約功能、補充首頁介面(侑宸)

v0.38：教練課程管理部分新增課程詳細介面(暫無修改功能)(侑宸)

v0.39：教練搜尋介面新增課程列表(侑宸)

v0.40：將Bitmap縮小避免卡頓(侑宸)

v0.41：教練首頁版面重編(侑宸)

v0.42：教練首頁暫時定案(侑宸)

v0.43：整理驗證函式、錯誤提示、用戶資料介面重編(侑宸)

v0.44：廣告函式整理(侑宸)

v0.45：忘記密碼傳送內容與Web統一、新增用戶資料確認載入特效、修復廣告顯示問題(侑宸)

v0.46：新增AI問答介面及服務(侑宸)

v0.47：使用者預約紀錄(尚未完成，尚在摸索中)(煒楷)

v0.48：使用者預約紀錄(狀態已取消完成)(煒楷)

v0.49：補充登入介面、驗證項整合，準備除錯匿名登入(侑宸)

v0.50：使用者預約紀錄完成(尚未做前往評論的部分)(煒楷)

v0.51：使用者預約紀錄+使用者評分皆完成(煒楷)

v0.52：使用者-我的收藏完成(看更多還沒做)(煒楷)

v0.53：修復用戶資料介面性別顯示問題(侑宸)

v0.54：教練資料介面同步用戶資料介面(侑宸)

v0.55：用戶首頁同步教練首頁(侑宸)

v0.56：AI介面重整，準備加入歷史訊息查看(侑宸)

v0.57：修復評論完，圖示沒變的bug(煒楷)

v0.58：所有課程(初版，未完成)(煒楷)

v0.59：所有課程(時段尚未完成)(煒楷)

v0.60：使用者查看所有課程+預約完成(煒楷)

v0.61：使用者所有教練完成(使用者教練頁編輯、刪除評論尚未做)(煒楷)

v0.62：使用者所有教練完成(接下來會做篩選)(煒楷)

v0.63：修改使用者首頁介面案紐改為圖片(侑宸)

v0.64：以Google Ads 替換原有廣告，並新增聯絡我們介面(侑宸)

v0.65：加入Mapbox顯示「聯絡我們」的地圖，還原教練首頁版面、更新依賴項(侑宸)

v0.66：準備建構導航功能(侑宸)

v0.67：篩選功能初版-未完成(煒楷)

v0.68：教練課程排班-草版(宇哲)

v0.69：修改使用者首頁介面、暫時移除匿名登入，導航功能研究失敗(侑宸)

v0.70：修正廣告尺寸、新增地圖放大縮小功能(侑宸)

v0.71：篩選功能完成，搜尋之後補(煒楷)

v0.72：教練首頁草版，各項功能未完善(宇哲)

v0.73：教練新增課程完成&教練查看課程待修改(宇哲)

v0.74：搜尋、重置完成，即將前往教練評論區(煒楷)

v0.75：修復重置沒有重置縣市、行政區的checkbox功能，即將前往教練評論區(煒楷)

v0.76：暫放(煒楷)

v0.77：補充README.md、嘗試初步構建店家列表及導航功能，並放在地圖模組中、初步嘗試整合兩個模組(侑宸)

v0.78：教練評價管理完成，準備做教練預約管理(煒楷)

v0.79：簡單處理coach_class_item跑版(侑宸)

v0.80：模組部分整合完成，可查看課程詳情、部分版面微調(侑宸)

v0.81：小修User_Class_Detail(侑宸)

v0.82：教練預約管理(尚未做查看預約名單)(煒楷)

v0.83：整理地圖模組(侑宸)

v0.84：教練預約管理完成(煒楷)

v0.85：嘗試解決「java.sql.SQLException: Invalid state, the Connection object is closed.」錯誤(侑宸)

v0.86：教練課程完成&各頁面標頭調整&班表還需要調整(宇哲)

v0.87：嘗試解決「java.sql.SQLException: I/O Error: Connection reset」錯誤 準備提供:
mapview模組連接資料庫功能(侑宸)

v0.88：完成開啟「目前位置到課程地點」的導航路線、初步更改app圖標、整理部分類的命名(侑宸)

v0.89：修復課程詳情圖片載入失敗問題及返回失效問題(侑宸)

v0.90：教練刪除課程完成&起始頁面調整&教練班表週曆研究中(宇哲)

v0.91：教練班表週曆完成&待加入月曆選日&班表新增待完善(宇哲)

v0.92：教練班表週曆月曆選日完成，剩餘班表新增待完善(宇哲)

v0.93：教練預約管理刷新修復(煒楷)

v0.94：教練班表新增完善，剩下其餘版面及小細節須修正(宇哲)

v0.95：忘記密碼修正(煒楷)

v0.96：新增班表加入合約到期日限制，首頁待修(宇哲)

v0.97：使用者個人資訊拍照功能(侑宸)

v0.98：整理Gradle檔,Manifest,檔案名稱,部分檔案內容、地圖介面初步運作(侑宸)

v0.99：無課程時段禁用日期、檢舉功能、最大最小值判斷皆完成，預計加入SharedPreferences來確保系統崩潰時，不會導致登出(煒楷)

v0.100：修復從google map回來導致的bug，看更多課程ID及看更多教練ID改為SharedPreferences的方式代替intent及bundle(煒楷)

v0.101：刪除課表通知及24H限制條件已完成(宇哲)

v0.102：使用者首頁地圖部分能用(侑宸)

v0.103：教練個人資料版面OK，取消預約有條件限制+通知(宇哲)

v0.104：教練首頁cardview暫放(宇哲)


## 補充：

### 圖片：

R.drawable.main_login_ic_account：預設使用者圖片

### Android Studio 操作

1. 快速排版程式碼：Code/Reformat Code
2. 解決部分編譯錯誤：Build/Clean Project->Build/Rebuild Project File/Invalid Caches/Restart
3. 更改sdk環境：Gradle Scripts/local.properties sdk.dir
4. 更改專案版本號：Gradle Scripts/build.gradle
5. 更改引入套件：Gradle Scripts/build.gradle
6. 更改套件版本：Gradle Scripts/libs.versions.toml
7. 快速查詢：Ctrl+F/Ctrl+Shift+F

### Android Studio 程式

1. `extends AsyncTask<Void, Void, Void>`為已棄用方法，請改用
   【`Executors.newSingleThreadExecutor().execute(() -> {
   //Background work here
   new Handler(Looper.getMainLooper()).post(() -> {
   //UI Thread work here
   }); });`】方法
2. 各個Activity中的ID請注意分別取不同名字，避免互相干擾，建議取名方式為「layout名稱_
   +元件功能+元件類型」，例：忘記密碼介面帳號的輸入框id:「Forget_accountEdit」
   當確認帳密無誤時可發送驗證碼，並將enable關掉，並隱藏帳密欄位顯示重設欄位
3. 要使用google提供的javamail-android發送電子郵件首先需要Google帳戶啟用兩步驟驗證->
   取得應用程式密碼->使用此密碼登入JavaMailAPI
4. 當遇到「It will always be more efficient to use more specific change events if you can. Rely on
   notifyDataSetChanged as a last resort.」問題
   請將目前使用的RecycleView更新方法從`notifyDataSetChanged()`方法，
   改成使用`notifyItemRangeChanged(0, adList.size())`
5. 如果出現「SQL Connection: Network error IOException: Socket closed」，可試試看延長逾期時間
6. 當愈到「The emulator process for AVD Pixel_4_API_30 has terminated.」問題，可嘗試更新NVIDIA驅動程式
7. 如果遇到AGP版本問題，請將專案的AGP版本與Android Studio可支援AGP版本調整至對應
8. 當遇到「'getAdapterPosition()' is deprecated」，把`getAdapterPosition()`
   改成`getBindingAdapterPosition()`
9. 使用Fragment套件時，ID取名不可用中文
10. 當遇到「Call to 'printStackTrace()' should probably be replaced with more robust logging」，請將
    `e.printStackTrace()`改成`Log.e("MapActivity", "Error fetching directions: " + e.getMessage());`
11. 如果要使用兩個按鈕評分水平位置，可使用create horizontal chains選項
12. 如果要使用使頁面可隨大小變化，可使用guideline並設定%比例
### Github 部分

1. 準備上傳至Github時，先點擊左上角「main」->「Update Project」，避免覆蓋掉他人部分

### 補充參考資料

[GitHub Markdown Tutorial](https://github.com/shengcaishizhan/GitHub_Markdown_Tutorial?tab=readme-ov-file#Markdown-%E6%96%87%E6%A1%A3%E5%A6%82%E4%BD%95%E5%88%B6%E4%BD%9C%E7%9B%AE%E5%BD%95)
