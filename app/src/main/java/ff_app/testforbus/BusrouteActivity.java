package ff_app.testforbus;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.autonavi.ae.route.route.Route;

import java.util.ArrayList;
import java.util.List;

public class BusrouteActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener,
        Inputtips.InputtipsListener, RouteSearch.OnRouteSearchListener {

    private MapView mMapView;
    private AMap aMap;

    private String citycode = "0571";
    private int nightflag = 0;//是否计算夜班车，默认为不计算，0：不计算，1：计算
    private RouteSearch routeSearch = null;

    private int currentpage = 0;// POI搜索当前页，第一页从0开始
    private List<PoiItem> poiItems = null;
    private String deepType = "交通设施|政府机构及社会团体|科教文化|金融保险|餐饮|购物";

    private PoiSearch.Query query;
    private LatLonPoint start = null;
    private LatLonPoint end = null;

    private EditText edit1;
    private EditText edit2;

    private boolean swill = false;
    private boolean ewill = false;

    private List<BusPath> buspathItems = null;
    private List<BusStep> bussteps = null;

    private ListView listView;
    private TextView routeinfo;

    /*
        地图初始化
     */
    private void init() {
        if (aMap == null) {
            // 显示地图
            aMap = mMapView.getMap();
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            setUpBlue();
        }
    }

    /*
        设置定位小蓝点
     */
    private void setUpBlue() {
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.getUiSettings().setScaleControlsEnabled(true);//比例尺
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busroute);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
        bombtninit();
        routeinfo = (TextView) findViewById(R.id.routeinfo);
        routeinfo.setVisibility(View.GONE);
        edit1 = (EditText) findViewById(R.id.startpoint);
        edit2 = (EditText) findViewById(R.id.endpoint);
        Button btn1 = (Button) findViewById(R.id.Search2);
        Button btn2 = (Button) findViewById(R.id.Search3);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = edit1.getText().toString().trim();
                if (keyword.equals("")) {
                    Toast.makeText(BusrouteActivity.this, "请输入起点", Toast.LENGTH_SHORT).show();
                } else {
                    swill = true;
                    ewill = false;
                    autofill(keyword, citycode);
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = edit2.getText().toString().trim();
                if (keyword.equals("")) {
                    Toast.makeText(BusrouteActivity.this, "请输入起点", Toast.LENGTH_SHORT).show();
                } else {
                    ewill = true;
                    swill = false;
                    autofill(keyword, citycode);
                }
            }
        });
        Button btn3 = (Button) findViewById(R.id.Search4);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start != null && end != null) {
                    routeset(citycode);
                } else {
                    Toast.makeText(BusrouteActivity.this, "不行啦", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        listView=(ListView)findViewById(R.id.listview);
        init();
    }

    private void autofill(String keyword, String citycode) {
        currentpage = 0;
        query = new PoiSearch.Query(keyword, deepType, citycode);//keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageNum(currentpage);
        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {
                if (result.getQuery().equals(query)) {
                    poiItems = result.getPois();
                    if (poiItems != null && poiItems.size() > 0) {
                        String[] sites = new String[poiItems.size()];
                        for (int i = 0; i < poiItems.size(); i++) {
                            sites[i] = poiItems.get(i).toString();
                        }
                        if (swill) {
                            showchoose1(sites);
                            swill = false;
                        } else if (ewill) {
                            showchoose2(sites);
                            ewill = false;
                        }

                    }

                }
            } else {
                Toast.makeText(this, "没找到", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "出错啦", Toast.LENGTH_SHORT).show();
        }
    }

    private void showchoose1(final String[] sites) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(BusrouteActivity.this);
        builder.setTitle("请选择起点");
        builder.setSingleChoiceItems(sites, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                start = poiItems.get(which).getLatLonPoint().copy();
                edit1.setText(sites[which]);
                swill = true;
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("返回", null);
        builder.show();
    }

    private void showchoose2(final String[] sites) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(BusrouteActivity.this);
        builder.setTitle("请选择终点");
        builder.setSingleChoiceItems(sites, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                end = poiItems.get(which).getLatLonPoint().copy();
                edit2.setText(sites[which]);
                ewill = false;
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("返回", null);
        builder.show();
    }

    private void routeset(String citycode) {
        routeSearch = new RouteSearch(BusrouteActivity.this);
        routeSearch.setRouteSearchListener(this);
        RouteSearch.FromAndTo fromandto = new RouteSearch.FromAndTo(start, end);
        RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromandto, RouteSearch.BusLeaseWalk, citycode, 0);
        routeSearch.calculateBusRouteAsyn(query);
    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getPaths().size() > 0) {
                aMap.clear();// 清理地图上的所有覆盖物
                buspathItems = result.getPaths();
                bussteps = buspathItems.get(0).getSteps();
                semarker(start);
                semarker(end);
                for (int i = 0; i < bussteps.size(); i++) {
                    if (bussteps.get(i).getWalk() != null) {
                        List<LatLng> lwalk = new ArrayList<LatLng>();
                        LatLonPoint a = bussteps.get(i).getWalk().getOrigin();
                        lwalk.add(new LatLng(a.getLatitude(), a.getLongitude()));
                        a = bussteps.get(i).getWalk().getDestination();
                        lwalk.add(new LatLng(a.getLatitude(), a.getLongitude()));
                        walkline(lwalk);
                    }
                    if (bussteps.get(i).getBusLines() != null) {
                        List<LatLng> lbus = new ArrayList<LatLng>();
                        System.out.println("yyy");
                        if(bussteps.get(i).getBusLines().size()>0) {
                            List<LatLonPoint> polybus = bussteps.get(i).getBusLines().get(0).getPolyline();
                            System.out.println("xxxx");
                            for (int j = 0; j < polybus.size(); j++) {
                                lbus.add(new LatLng(polybus.get(j).getLatitude(), polybus.get(j).getLongitude()));
                            }
                            busline(lbus);
                        }
                    }
                }
                showinfo();
            }
        } else {
            Toast.makeText(BusrouteActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
        }
    }

    private void semarker(LatLonPoint a) {
        LatLng l = new LatLng(a.getLatitude(), a.getLongitude());
        Marker m = aMap.addMarker(new MarkerOptions().position(l));
    }

    private void busline(List<LatLng> l) {
        PolylineOptions po = new PolylineOptions().addAll(l).width(10)
                .setDottedLine(false).color(Color.argb(255, 1, 1, 1));
        mMapView.getMap().addPolyline(po);
    }

    private void walkline(List<LatLng> l) {
        PolylineOptions po = new PolylineOptions().addAll(l).width(10)
                .setDottedLine(false).color(Color.argb(255, 0, 174, 239));
        mMapView.getMap().addPolyline(po);
    }

    private void showinfo() {
        StringBuffer a = new StringBuffer();
        a.append("步行距离：").append(buspathItems.get(0).getWalkDistance()).append('m').append('\n');
        int q=0;
        for (int i = 0; i < bussteps.size(); i++) {
            if(bussteps.get(i).getBusLines().size()>0) {
//                a.append(q + 1).append('.');
                a.append(bussteps.get(i).getBusLines().get(0).getBusLineName()).append('\n');
                a.append(bussteps.get(i).getBusLines().get(0).getDepartureBusStation().getBusStationName());
                a.append("---");
                a.append(bussteps.get(i).getBusLines().get(0).getArrivalBusStation().getBusStationName());
                a.append('\n');
//                q++;
            }
        }
        System.out.println(a);
        routeinfo.setText(a);
        routeinfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        routeinfo.setVisibility(View.VISIBLE);
    }

    private void bombtninit() {
        Button btn1 = (Button) findViewById(R.id.bombtn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent();
                it.setClass(BusrouteActivity.this, MainActivity.class);
                startActivity(it);
                BusrouteActivity.this.finish();
            }
        });
        Button btn2 = (Button) findViewById(R.id.bombtn2);
        Button btn3 = (Button) findViewById(R.id.bombtn3);
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
        mMapView.onResume();
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

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int rCode) {

    }


    @Override
    public void onGetInputtips(List<Tip> list, int i) {

    }


    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }
}
