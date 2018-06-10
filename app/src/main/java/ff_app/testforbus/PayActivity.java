package ff_app.testforbus;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class PayActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_pay);

                WebView webView = new WebView(this);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadUrl("http://wxpay.weixin.qq.com/pub_v2/pay/wap.v2.php");

    }

    public void onClick(View view) {
        Uri uri = Uri.parse("https://mobile.alipay.com/index.htm");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


}
