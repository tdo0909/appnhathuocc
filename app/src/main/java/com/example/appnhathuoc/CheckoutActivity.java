package com.example.appnhathuoc;

import com.example.appnhathuoc.utils.BottomNavigationHelper;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appnhathuoc.model.CartItem;
import com.example.appnhathuoc.model.Order;
import com.example.appnhathuoc.utils.CartManager;
import com.example.appnhathuoc.utils.OrderManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etName, etPhone, etCity, etDistrict, etWard, etAddressDetail;
    private RadioGroup rgPayment;
    private RadioButton rbCod, rbBank;
    private LinearLayout layoutItemsContainer;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private Button btnPlaceOrder;

    private CartManager cartManager;
    private OrderManager orderManager;
    private List<CartItem> cartItems;
    private DecimalFormat priceFormatter = new DecimalFormat("#,###đ");

    private double subtotal = 0;
    private double shippingFee = 0;
    private double total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        BottomNavigationHelper.attach(this, R.id.navigation_category);

        cartManager = CartManager.getInstance(this);
        orderManager = OrderManager.getInstance(this);

        cartItems = cartManager.getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        renderOrderItems();
        calculateTotal();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_checkout);
        etName = findViewById(R.id.et_checkout_name);
        etPhone = findViewById(R.id.et_checkout_phone);
        etCity = findViewById(R.id.et_checkout_city);
        etDistrict = findViewById(R.id.et_checkout_district);
        etWard = findViewById(R.id.et_checkout_ward);
        etAddressDetail = findViewById(R.id.et_checkout_address_detail);
        rgPayment = findViewById(R.id.rg_payment_method);
        rbCod = findViewById(R.id.rb_payment_cod);
        rbBank = findViewById(R.id.rb_payment_bank);
        layoutItemsContainer = findViewById(R.id.layout_checkout_items_container);
        tvSubtotal = findViewById(R.id.tv_checkout_subtotal);
        tvShipping = findViewById(R.id.tv_checkout_shipping);
        tvTotal = findViewById(R.id.tv_checkout_total);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
    }

    private void renderOrderItems() {
        layoutItemsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CartItem item : cartItems) {
            if (item.getProduct() == null) continue;
            View view = inflater.inflate(R.layout.item_cart, layoutItemsContainer, false);

            ImageView img = view.findViewById(R.id.img_cart_product);
            TextView tvName = view.findViewById(R.id.tv_cart_product_name);
            TextView tvPrice = view.findViewById(R.id.tv_cart_product_price);
            TextView tvQty = view.findViewById(R.id.tv_cart_item_qty);
            TextView tvSub = view.findViewById(R.id.tv_cart_item_subtotal);
            TextView btnDelete = view.findViewById(R.id.btn_delete_cart_item);
            View minus = view.findViewById(R.id.btn_cart_minus);
            View plus = view.findViewById(R.id.btn_cart_plus);

            if (item.getProduct().getImageUrl() != null && !item.getProduct().getImageUrl().isEmpty()) {
                com.example.appnhathuoc.utils.ImageUtils.loadImage(img, item.getProduct().getImageUrl(), item.getProduct().getImageResId());
            } else {
                img.setImageResource(item.getProduct().getImageResId());
            }

            tvName.setText(item.getProduct().getName());
            tvPrice.setText(priceFormatter.format(item.getProduct().getPrice()));
            tvQty.setText("x" + item.getQuantity());
            tvSub.setText(priceFormatter.format(item.getTotalPrice()));

            // Hide editable controls in checkout list
            btnDelete.setVisibility(View.GONE);
            minus.setVisibility(View.GONE);
            plus.setVisibility(View.GONE);

            layoutItemsContainer.addView(view);
        }
    }

    private void calculateTotal() {
        subtotal = cartManager.getTotalAmount();
        shippingFee = subtotal >= 300000 ? 0 : 15000;
        total = subtotal + shippingFee;

        tvSubtotal.setText(priceFormatter.format(subtotal));
        if (shippingFee == 0) {
            tvShipping.setText("Miễn phí");
        } else {
            tvShipping.setText(priceFormatter.format(shippingFee));
        }
        tvTotal.setText(priceFormatter.format(total));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> processCheckout());
    }

    private void processCheckout() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String ward = etWard.getText().toString().trim();
        String addressDetail = etAddressDetail.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Vui lòng nhập họ tên");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(city)) {
            etCity.setError("Vui lòng nhập tỉnh / thành phố");
            etCity.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(district)) {
            etDistrict.setError("Vui lòng nhập quận / huyện");
            etDistrict.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(ward)) {
            etWard.setError("Vui lòng nhập phường / xã");
            etWard.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(addressDetail)) {
            etAddressDetail.setError("Vui lòng nhập địa chỉ cụ thể");
            etAddressDetail.requestFocus();
            return;
        }

        String fullAddress = addressDetail + ", " + ward + ", " + district + ", " + city;
        String paymentMethod = rbCod.isChecked() ? "Thanh toán khi nhận hàng (COD)" : "Chuyển khoản ngân hàng";
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        try {
            org.json.JSONObject orderJson = new org.json.JSONObject();
            orderJson.put("fullName", name);
            orderJson.put("full_name", name);
            orderJson.put("phone", phone);
            orderJson.put("address", fullAddress);
            orderJson.put("paymentMethod", paymentMethod);
            orderJson.put("payment_method", paymentMethod);
            orderJson.put("subtotal", subtotal);
            orderJson.put("shippingFee", shippingFee);
            orderJson.put("shipping_fee", shippingFee);
            orderJson.put("totalAmount", total);
            orderJson.put("total_amount", total);
            orderJson.put("date", currentDate);
            orderJson.put("userPhone", com.example.appnhathuoc.utils.UserManager.getInstance(this).getUserPhone());

            org.json.JSONArray itemsJson = new org.json.JSONArray();
            for (CartItem ci : cartItems) {
                if (ci.getProduct() != null) {
                    org.json.JSONObject itemObj = new org.json.JSONObject();
                    itemObj.put("productId", ci.getProduct().getId());
                    itemObj.put("productCode", ci.getProduct().getId());
                    itemObj.put("product_name", ci.getProduct().getName());
                    itemObj.put("name", ci.getProduct().getName());
                    itemObj.put("price", ci.getProduct().getPrice());
                    itemObj.put("quantity", ci.getQuantity());
                    itemsJson.put(itemObj);
                }
            }
            orderJson.put("items", itemsJson);

            btnPlaceOrder.setEnabled(false);
            com.example.appnhathuoc.api.ApiClient.placeOrder(orderJson, new com.example.appnhathuoc.api.ApiClient.ApiCallback<org.json.JSONObject>() {
                @Override
                public void onSuccess(org.json.JSONObject result) {
                    String orderCode = result == null
                            ? ""
                            : result.optString("order_code", result.optString("orderId", ""));
                    if (orderCode.trim().isEmpty()) {
                        btnPlaceOrder.setEnabled(true);
                        Toast.makeText(CheckoutActivity.this, "Đơn hàng đã gửi nhưng không nhận được mã đơn. Vui lòng làm mới danh sách đơn hàng.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Order newOrder = new Order(
                            orderCode,
                            currentDate,
                            cartItems,
                            total,
                            "Chờ xác nhận",
                            name,
                            phone,
                            fullAddress,
                            paymentMethod
                    );

                    orderManager.addOrder(newOrder);
                    cartManager.clearCart();

                    showSuccessDialog(name, orderCode);
                }

                @Override
                public void onError(String errorMessage) {
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccessDialog(String name, String orderCode) {
        View successView = getLayoutInflater().inflate(R.layout.dialog_order_success, null);
        TextView tvGreeting = successView.findViewById(R.id.tv_success_greeting);
        TextView tvOrderCode = successView.findViewById(R.id.tv_success_order_code);
        TextView tvTotal = successView.findViewById(R.id.tv_success_total);
        tvGreeting.setText("Cảm ơn " + name + " đã đặt hàng tại nhà thuốc.");
        tvOrderCode.setText(orderCode);
        tvTotal.setText(priceFormatter.format(total));

        new AlertDialog.Builder(this)
                .setView(successView)
                .setPositiveButton("Xem đơn hàng", (dialog, which) -> {
                    Intent intent = new Intent(CheckoutActivity.this, OrdersActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Về trang chủ", (dialog, which) -> {
                    Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
