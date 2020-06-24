package com.hms.ias;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

class ProductItem extends Object {
    String id = "";
    String name = "";
    String description = "";
    String price = "";
    int priceType = 0;
}

class ProductAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<ProductItem> items;

    public ProductAdapter(Context context, ArrayList<ProductItem> items) {
        super(context, -1, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.product_view, parent, false);

        ProductItem item = this.items.get(position);;

        TextView textProdId = rowView.findViewById(R.id.txtId);
        TextView textProdName = rowView.findViewById(R.id.txtName);
        TextView textProdDesc = rowView.findViewById(R.id.txtDesc);
        TextView textProdPrice = rowView.findViewById(R.id.txtPrice);

        textProdId.setText("ID: " + item.id);
        textProdName.setText("NAME: " + item.name);
        textProdDesc.setText("DESCRIPTION : " + item.description);
        textProdPrice.setText("PRICE: " + item.price);

        return rowView;
    }
}

public class MainActivity extends AppCompatActivity {

    private final String publicKey = "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEA257+qq89jiz3O2j6RZDEysoiVLgzVUtCIFrPZKv5pUtYHpYExdFUjhRQTCAqQTbxCtMJH0JkSJrNz/kOWfKuDTcMvy9yWTCFp7x97MEswq2vNNxUbMnpRxyuUQMph6J0P5rQuflP/0O5m9axvCBQ5SDGiHERJZeZxw2/Aj0yYYFESs59DsBdt6sqmyJfiEZ/CQiOSCLTiscpzAnbWBO3qBrGOLQj1YIeLvJb/sjyHPmVXGdfa473hsr9h1yOGA1qCnMlbIOF5z5UNwbwMRvFyiE/PT/hrYMzsWR9bB0j4HUSTXEXIQrwoXbtH8+ICdjyVRdap2hq4nGLS/GkKFdaYsEVWnQ1cpMTOH4Fu2DueTlYZJw/Ql1/XUjoz3P48U9Oo0IQIB1SIngXMuhZuhjM83oXs4U3uxCRYnSKs1txmKvIZbBUSfRVAXshcSvD3ivaEdmUGqL28bCrX0zFyQ8LXmKhy9Wb8XLctrtpjb27SpifjfTjzMt2NDFmm7LJGY5jAgMBAAE=";
    private int REQUEST_SIGN_IN_LOGIN = 1002;

    ListView listView;

    TextView txtProdId;
    TextView txtProdName;
    TextView txtProdDesc;
    TextView txtProdPrice;
    Button buttonPurchase;
    Button buttonSignIn;
    HuaweiIdAuthService mHuaweiIdAuthService;

    Boolean isLoggedIn = false;

    int REQ_CODE_BUY = 4002;

    ArrayList<ProductItem> productList = new ArrayList<>();
    private ProductItem selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if( productList.toArray().length > 0) {
                    ProductItem item = productList.get(position);
                    displayProduct(item);
                }
            }
        });

        buttonPurchase = findViewById(R.id.buttonPurchase);
        buttonPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!selectedItem.equals(null)) {
                    purchaseItem(selectedItem);
                }
            }
        });

        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLoggedIn) {
                    signIn();
                } else {
                    fetchProduct();
                }
            }
        });

        txtProdId = findViewById(R.id.detailId);
        txtProdName = findViewById(R.id.detailName);
        txtProdDesc = findViewById(R.id.detailDesc);
        txtProdPrice = findViewById(R.id.detailPrice);

        buttonPurchase.setEnabled(false);

    }

    void signIn() {

        HuaweiIdAuthParams mHuaweiIdAuthParams;
        mHuaweiIdAuthParams = new HuaweiIdAuthParamsHelper (HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setIdToken().createParams();

        mHuaweiIdAuthService = HuaweiIdAuthManager.getService (this, mHuaweiIdAuthParams);
        startActivityForResult(mHuaweiIdAuthService.getSignInIntent(), REQUEST_SIGN_IN_LOGIN);

    }

    ProductInfoReq productRequest(int priceType) {
        ProductInfoReq productInfoReq = new ProductInfoReq();
        productInfoReq.setPriceType(priceType);
        ArrayList<String> productIds = new ArrayList<>();
        productIds.add("ITEM-001");
        productIds.add("ITEM-002");
        productIds.add("ITEM-003");
        productIds.add("ITEM-004");
        productInfoReq.setProductIds(productIds);
        return productInfoReq;
    }

    void fetchProduct() {
        IapClient inAppClient  = Iap.getIapClient(this);

        Task<ProductInfoResult> task = inAppClient.obtainProductInfo(productRequest(IapClient.PriceType.IN_APP_CONSUMABLE));

        task.addOnSuccessListener(new OnSuccessListener<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult productInfoResult) {
                if (productInfoResult != null && !productInfoResult.getProductInfoList().isEmpty()) {
                    loadProduct(productInfoResult.getProductInfoList());
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {

                if (e instanceof IapApiException) {
                    IapApiException error = (IapApiException) e;

                    int errorCode = error.getStatusCode();

                    if (errorCode == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                        Toast.makeText( MainActivity.this,"Please sign in with your HUAWEI credential", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText( MainActivity.this, "In-App Purchase fetch error " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText( MainActivity.this, "In-App Purchase operation error", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    void loadProduct( List<ProductInfo> inAppproductList) {

        for( ProductInfo inAppItem : inAppproductList) {

            ProductItem item = new ProductItem();

            item.id = inAppItem.getProductId();
            item.name = inAppItem.getProductName();
            item.description = inAppItem.getProductDesc();
            item.price = inAppItem.getPrice();
            item.priceType = inAppItem.getPriceType();

            productList.add(item);

        }

        ProductAdapter productAdapter = new ProductAdapter(this, productList);

        listView.setAdapter(productAdapter);
    }

    void displayProduct(ProductItem item) {
        selectedItem = item;

        txtProdId.setText( getResources().getString(R.string.prodId) + selectedItem.id);
        txtProdName.setText(getResources().getString(R.string.prodName) + selectedItem.name);
        txtProdDesc.setText(getResources().getString(R.string.prodDesc) + selectedItem.description);
        txtProdPrice.setText(getResources().getString(R.string.prodPrice) + selectedItem.price);

        buttonPurchase.setEnabled(true);

    }

    void purchaseItem(ProductItem item) {

        final Activity activity = this;
        IapClient purchaseClient = Iap.getIapClient(activity);

        PurchaseIntentReq purchaseRequest = new PurchaseIntentReq();
        purchaseRequest.setProductId(item.id);
        purchaseRequest.setPriceType(item.priceType);
        purchaseRequest.setDeveloperPayload("test");

        Task<PurchaseIntentResult> purchaseTask = purchaseClient.createPurchaseIntent(purchaseRequest);

        purchaseTask.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult purchaseIntentResult) {

                if (purchaseIntentResult.equals(null)) {
                    Toast.makeText(activity, "Result is null", Toast.LENGTH_LONG).show();

                    return;
                }


                Status status = purchaseIntentResult.getStatus();

                if (status.equals(null)) {
                    Toast.makeText(activity, "Status is null", Toast.LENGTH_LONG).show();
                    return;
                }

                if (status.hasResolution()) {
                    try {
                        status.startResolutionForResult(activity, REQ_CODE_BUY);
                    } catch (IntentSender.SendIntentException exp) {
                        Toast.makeText(activity, exp.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(activity, "Resolution is false", Toast.LENGTH_LONG).show();
                }



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    Toast.makeText(MainActivity.this, apiException.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE_BUY) {

            if( data.equals(null)) {
                Toast.makeText(this, "Data is null", Toast.LENGTH_LONG).show();
                return;
            }

            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    // verify signature of payment results.
                    boolean success = CipherUtil.doCheck(purchaseResultInfo.getInAppPurchaseData(), purchaseResultInfo.getInAppDataSignature(), publicKey);
                    if (success) {

                        finalizePurchase(purchaseResultInfo);

                    } else {
                        Toast.makeText(this, "Payment successful, Sign-in failed", Toast.LENGTH_LONG).show();

                    }
                    return;
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Toast.makeText(this, "User cancel payment", Toast.LENGTH_LONG).show();
                    return;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:

                    Toast.makeText(this, "User already purchased the product", Toast.LENGTH_LONG).show();
                    return;

                default:
                    Toast.makeText(this, "Payment failed", Toast.LENGTH_LONG).show();
                    break;
            }
            return;
        }

        if (requestCode == REQUEST_SIGN_IN_LOGIN) {

            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager. parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                isLoggedIn = true;

                buttonSignIn.setText(getResources().getString(R.string.btnSignedIn)+ " as " + huaweiAccount.getDisplayName());
                fetchProduct();
            } else {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    void finalizePurchase(PurchaseResultInfo purchaseInfo) {
        final Context context = this;
        IapClient purchaseClient = Iap.getIapClient(context);

        ConsumeOwnedPurchaseReq request = new ConsumeOwnedPurchaseReq();

        try {
            InAppPurchaseData purchaseData = new InAppPurchaseData(purchaseInfo.getInAppPurchaseData());
            request.setPurchaseToken(purchaseData.getPurchaseToken());
        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        Task<ConsumeOwnedPurchaseResult> purchaseTask = purchaseClient.consumeOwnedPurchase(request);

        purchaseTask.addOnSuccessListener(new OnSuccessListener<ConsumeOwnedPurchaseResult>() {
            @Override
            public void onSuccess(ConsumeOwnedPurchaseResult consumeOwnedPurchaseResult) {
                Toast.makeText(context, "Pay success, and the product has been delivered", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {

                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    Toast.makeText(context, apiException.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
