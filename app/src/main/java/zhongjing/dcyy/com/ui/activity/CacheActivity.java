package zhongjing.dcyy.com.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import zhongjing.dcyy.com.R;
import zhongjing.dcyy.com.bean.CacheVideoInfo;
import zhongjing.dcyy.com.ui.view.CustomDialog;
import zhongjing.dcyy.com.utils.DateUtils;
import zhongjing.dcyy.com.utils.FileUtils;

public class CacheActivity extends BaseActivity {
    public static final String CACHE_VIDEO_URL = "cache_video_url";
    public   static final String   CACHE_VIDEO_PTAH ="filename";
    private List<CacheVideoInfo> videoInfos = new ArrayList<>();

    private SwipeMenuListView listView;
    private ListViewAdapter adapter;
    private Button btn_sreach;
    private int curyid;
    private Spinner spinner;
    private SearchView sreach_view;
    private ProgressDialog progressDialog;

    private static final String SAVE_PIC_PATH = Environment.getExternalStoragePublicDirectory("").getAbsolutePath();//保存到SD卡
    public static final String SAVE_REAL_PATH = SAVE_PIC_PATH + "/LVAS";//保存的确切位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache);
        initData();
        initView();
        initEvent();
        initListener();
        setCursListener(0,true);
        sreach_view.setIconifiedByDefault(true);
        sreach_view.onActionViewExpanded();
        sreach_view.setSubmitButtonEnabled(false); //显示搜索按钮
        sreach_view.clearFocus();

        File  file= new File(SAVE_REAL_PATH);
        if(!file.exists()){
            file.mkdir();
        }
    }


//    private static final String SAVE_PIC_PATH= Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡
//    public static final String SAVE_REAL_PATH = SAVE_PIC_PATH+ "/zhongjingVideo";//保存的确切位置

    private void initData() {
        File dir = new File( SAVE_REAL_PATH);
        File[] files = dir.listFiles();
        if(files==null){
            return;
        }
        for (File file : files) {
            CacheVideoInfo videoInfo = new CacheVideoInfo();
            /*MediaMetadataRetriever mmr=new MediaMetadataRetriever();
            mmr.setDataSource(file.getPath());
            //获取第一帧图像的bitmap对象
            Bitmap bitmap=mmr.getFrameAtTime();*/
            long fileSize = FileUtils.getFileSize(file);
            String filename = file.getName();
            String name = filename.substring(0, 13);
            videoInfo.filePath=file.getAbsolutePath();
            //videoInfo.videoBitmap = bitmap;
            videoInfo.videoSize = fileSize;
            videoInfo.videoName = name;
            if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".png") ||file.getName().toLowerCase().endsWith(".jpeg")) {
                videoInfo.videoBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                videoInfo.isImage = 2;
            }else  if (file.getName().toLowerCase().endsWith(".mp3") || file.getName().toLowerCase().endsWith(".wav")) {
                videoInfo.videoBitmap = BitmapFactory.decodeResource( getApplicationContext().getResources(),R.drawable.ic_recording_file);
                videoInfo.isImage = 3;
            } else {
                videoInfo.isImage = 1;
                videoInfo.videoBitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            }
            videoInfos.add(videoInfo);
        }
    }

    private void initView() {
        listView = (SwipeMenuListView) findViewById(R.id.activity_cache_listView);
        btn_sreach = (Button) findViewById(R.id.btn_sreach1);
        sreach_view= (SearchView)  findViewById(R.id.sreach_view);
        btn_sreach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 *   <item>全部</item><item>视频</item>item>录音</item><item>图片</item>
                 */
               String text= (String) spinner.getSelectedItem();
               int  type = -1 ;
                 // 1 视频  2 图片  3 声音
               if("图片".equals(text)){
                   type=2;
               }else if("视频".equals(text)){
                   type=1;
               }else if( text.contains("录音")){
                   type=3;
               }
               //搜索
                List<CacheVideoInfo> list=new ArrayList<>();
                for (CacheVideoInfo cacheVideoInfo : videoInfos) {
                   if(type>-1 &&    cacheVideoInfo.isImage  ==type){
                       if(!TextUtils.isEmpty(sreach_view.getQuery())){
                          if(cacheVideoInfo.videoName.contains( sreach_view.getQuery())) {
                              list.add(cacheVideoInfo);
                          }
                       }else{
                           list.add(cacheVideoInfo);
                       }
                     }
                }
                if(type>-1){
                    adapter.videos=list;
                }else{
                    adapter.videos=videoInfos;
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void initEvent() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(300);
                deleteItem.setTitle(R.string.delete);
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);

            }
        };
        listView.setMenuCreator(creator);
        adapter = new ListViewAdapter(videoInfos);
        listView.setAdapter(adapter);

    }

    private void initListener() {
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index){
                    case 0:        //删除键
                      new  AlertDialog.Builder(CacheActivity.this).setMessage("是否删除文件?")
                              .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                               @Override
                               public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                               }
                        }).setPositiveButton("删除",  new DialogInterface.OnClickListener(){
                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                  CacheVideoInfo cacheVideoInfo = videoInfos.get(position);
                                  File deleteFile = new File(cacheVideoInfo.filePath );
                                  deleteFile.delete();           //删除文件
                                  videoInfos.remove(position);   //删除集合中的数据
                                  adapter.notifyDataSetChanged();
                              }
                         })    .create().show();
                        break;
                }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CacheVideoInfo cacheVideoInfo = videoInfos.get(position);

                if(cacheVideoInfo.isImage ==2){
//                    File file=new File(cacheVideoInfo.filePath);
//                    Intent it =new Intent(Intent.ACTION_VIEW);
//                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Uri mUri = Uri.fromFile(new File(cacheVideoInfo.filePath));
//                    it.setDataAndType(mUri, "image/*");
//                    it.addCategory("android.intent.category.DEFAULT");
//                    startActivity(it);
//                    Message msg= mHandle.obtainMessage(200);
//                    msg.obj=cacheVideoInfo.filePath;
//                    mHandle.sendMessage(msg);
                }else  if(cacheVideoInfo.isImage ==1){
                    File file=new File(cacheVideoInfo.filePath);
                    Intent cacheVideoIntent = new Intent(CacheActivity.this,CacheVideoActivity.class);
                    cacheVideoIntent.putExtra(CACHE_VIDEO_URL,cacheVideoInfo.videoName);
                    cacheVideoIntent.putExtra(CACHE_VIDEO_PTAH,file.getName());
                    startActivity(cacheVideoIntent);

                }else if(cacheVideoInfo.isImage ==3) {
                    new CustomDialog.Builder(CacheActivity.this).showProgressDialog(cacheVideoInfo.filePath).show();

                }
            }
        });
    }

    public void click(View view){
        switch (view.getId()){
            case R.id.activity_cache_back:  //返回键
                finish();
                break;
        }
    }

    class ListViewAdapter extends BaseAdapter{
        List <CacheVideoInfo>videos=new ArrayList();
        public ListViewAdapter( List<CacheVideoInfo> videoInfos ) {
            this.videos=videoInfos;
        }

        @Override
        public int getCount() {
            if(videos==null){
                return 0;
            }
            return videos.size();
        }

        @Override
        public CacheVideoInfo getItem(int position) {
            return videos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(CacheActivity.this, R.layout.item_cache_activity, null);
                holder = new ViewHolder();
                holder.videoPic = (ImageView) convertView
                        .findViewById(R.id.item_cache_activity_iv);
                holder.videoName = (TextView) convertView
                        .findViewById(R.id.item_cache_activity_tv_videoname);
                holder.videoSize = (TextView) convertView
                        .findViewById(R.id.item_cache_activity_tv_videosize);
                holder.videtype= (TextView) convertView
                        .findViewById(R.id.item_cache_activity_tv_videtype);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CacheVideoInfo bean = videos.get(position);
            String  str="";
            // 1 视频  2 图片  3 声音
            if(bean.isImage == 1 ) { //视频
                str="视频";
            }else if(bean.isImage == 2 ){ //图片
                str="图片";
            }else if(bean.isImage == 3 ){ //录音
                str="录音";
                if(bean.filePath.endsWith("wav") || bean.filePath.endsWith("WAV") ){
                    str+="-WAV";
                }else{
                    str+="-MP3";
                }
            }
            holder.videtype.setText(str);
            holder.videoPic.setImageBitmap(bean.videoBitmap);
            holder.videoName.setText(DateUtils.FormatDate(bean.videoName));
            holder.videoSize.setText(FileUtils.FormatFileSize(bean.videoSize));
            return convertView;
        }

        class ViewHolder {
            ImageView videoPic;
            TextView videoName;
            TextView videoSize;
            TextView  videtype;
        }
    }

    // 初始化完了后改变 Spinner文字标题颜色

    private void setCursListener(  int select, boolean monitor) {
         spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.layer_frends,getResources().getStringArray(R.array.languages));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(select);
        if(monitor){
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                    curyid = position;
                    //showPrice(position);
                    TextView tv = (TextView)view;
                    tv.setTextColor(Color.BLACK);    //设置颜色
                    tv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);   //设置居中
                    btn_sreach.callOnClick();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent){}
            });
        }
    }


    private Handler mHandle=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what ){
                case 200:{
                    Toast.makeText(CacheActivity.this,"文件路径:" + msg.obj , Toast.LENGTH_LONG).show();
                    File file=new File((String)msg.obj);
                    Intent it =new Intent(Intent.ACTION_VIEW);
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri mUri = Uri.fromFile(file);
                    it.setDataAndType(mUri, "image/*");
                    it.addCategory("android.intent.category.DEFAULT");
                    startActivity(it);
                }break;
                case 300:{
                    Toast.makeText(CacheActivity.this,"文件路径:" + msg.obj , Toast.LENGTH_LONG).show();
                    File file=new File((String)msg.obj);
                    Intent it =new Intent(Intent.ACTION_VIEW);
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri mUri =Uri.fromFile(file);
                    it.setDataAndType(mUri, "audio/*");
                    it.addCategory("android.intent.category.DEFAULT");
                    startActivity(it);
                }break;

            }
        }
    };





}
