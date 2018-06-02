package ff_app.testforbus;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MultiPointItem;
import com.amap.api.maps.model.MultiPointOverlayOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        BusLineSearch.OnBusLineSearchListener {

    private MapView mMapView;
    private AMap aMap;

    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            // Manifest.permission.READ_PHONE_STATE
    };

    private static final int thatgood= 2048;

    private String citycode="0571";

    private BusLineQuery busLineQuery;// 公交线路查询的查询类
    private int currentpage = 0;// 公交搜索当前页，第一页从0开始
    private BusLineResult busLineResult;// 公交线路搜索返回的结果
    private List<BusLineItem> lineItems = null;// 公交线路搜索返回的busline

    private void init() {
        if (aMap == null) {
            // 显示地图
            aMap = mMapView.getMap();
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            setUpBlue();
        }
    }


    private void setUpBlue() {
        //小蓝点设置
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.getUiSettings().setScaleControlsEnabled(true);//比例尺
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //定义了一个地图view
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
        if(!checkPermissions()){
            ActivityCompat.requestPermissions(MainActivity.this,needPermissions,thatgood);
        }
        init();
        final TextView busNameText=(TextView)findViewById(R.id.busName);
        Button searchbtn=(Button)findViewById(R.id.Search);
        searchbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String busName=busNameText.getText().toString().trim();
                if(busName.equals("")){
                    Toast.makeText(MainActivity.this,"请输入公交名称",Toast.LENGTH_SHORT).show();
                }else{
                    searchBusLine(busName,citycode);
                }
            }
        });

    }

    private void searchBusLine(String busName,String citycode){
        currentpage = 0;
        busLineQuery = new BusLineQuery(busName, BusLineQuery.SearchType.BY_LINE_NAME,citycode);
        // 第一个参数表示公交线路名，第二个参数表示公交线路查询，第三个参数表示所在城市名或者城市区号
        busLineQuery.setPageSize(10);// 设置每页返回多少条数据
        busLineQuery.setPageNumber(currentpage);// 设置查询第几页，第一页从0开始算起
        BusLineSearch busLineSearch = new BusLineSearch(this,busLineQuery);// 设置条件
        busLineSearch.setOnBusLineSearchListener(this);// 设置查询结果的监听
        busLineSearch.searchBusLineAsyn();// 异步查询公交线路名称
    }




    @Override
    public void onBusLineSearched(BusLineResult result, int rCode) {
        //解析result获取搜索结果
        if(rCode == AMapException.CODE_AMAP_SUCCESS){
            if (result != null && result.getQuery() != null
                    && result.getQuery().equals(busLineQuery)) {
                if (result.getQuery().getCategory() == BusLineQuery.SearchType.BY_LINE_NAME) {
                    if (result.getPageCount() > 0
                            && result.getBusLines() != null
                            && result.getBusLines().size() > 0) {
                        busLineResult = result;
                        lineItems = result.getBusLines();
                        if(lineItems != null) {
//                            for(int i=0;i<lineItems.size();i++){
//                                System.out.println(lineItems.get(i).getBusLineName());
//                            }
                            String[] lineNameString = new String[lineItems.size()];
                            for (int i = 0; i < lineItems.size(); i++) {
                                lineNameString[i] = lineItems.get(i).getBusLineName();
                            }
                            String[][] lineStationString = new String[lineItems.size()][];
                            for (int i = 0; i < lineItems.size(); i++) {
                                int size=lineItems.get(i).getBusStations().size();
                                lineStationString[i]=new String[size];
                                System.out.println(lineNameString[i]);
                                for(int j=0;j<size;j++){
                                    lineStationString[i][j]=lineItems.get(i).getBusStations().get(j).getBusStationName();
//                                    System.out.println(lineItems.get(i).getBusStations().get(j).getBusStationName());
//                                    System.out.println(lineStationString[i][j]);
                                }
                                System.out.println();
                            }
                            showChoose(lineNameString,lineStationString);
                        }


                    }
                }
            } else {
                Toast.makeText(this,"找不到！！！\n",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,"诶 失败了\n",Toast.LENGTH_SHORT).show();
        }
    }

    private void showChoose(String [] line, final String [][] station){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("请选择公交");
        builder.setSingleChoiceItems(line, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                AlertDialog.Builder builder1=new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("公交路线");
                StringBuffer a=new StringBuffer();
                for(int i=0;i<station[which].length;i++){
                    a.append(station[which][i]);
                    a.append('\n');
                }
                builder1.setMessage(a);
                builder1.setCancelable(true);
                builder1.setNegativeButton("返回",null);
                builder1.show();

            }
        });
        builder.setNegativeButton("返回",null);
        builder.show();
    }

    private boolean checkPermissions(){
        for(String permisson:needPermissions){
            if(ContextCompat.checkSelfPermission(MainActivity.this,permisson)!=
                    PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    private boolean verifyPermissions(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case thatgood:
                if (grantResults.length > 0 && verifyPermissions(grantResults)) {
                    //已授权
                    Toast.makeText(MainActivity.this, "您已开启权限,请重新打开软件",
                            Toast.LENGTH_SHORT).show();

                } else { //用户拒绝
                    Toast.makeText(MainActivity.this, "您已拒绝了开启权限,若想再访问请手动打开权限",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }

    }



}

