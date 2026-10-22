package com.example.appnhathuoc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appnhathuoc.adapter.OrderAdapter;
import com.example.appnhathuoc.model.CartItem;
import com.example.appnhathuoc.model.Order;
import com.example.appnhathuoc.utils.UserManager;
import com.example.appnhathuoc.utils.OrderManager;
import com.example.appnhathuoc.utils.BottomNavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private ImageView btnBack;
    private android.widget.TextView btnRefresh;
    private RecyclerView rvOrders;
    private LinearLayout layoutOrdersState;
    private TextView tvOrdersStateTitle, tvOrdersStateMessage;
    private android.widget.Button btnRetryOrders;
    private BottomNavigationView bottomNavigationView;
    private OrderAdapter adapter;
    private OrderManager orderManager;
    private DecimalFormat priceFormatter = new DecimalFormat("#,###đ");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        orderManager = OrderManager.getInstance(this);
        if (!com.example.appnhathuoc.utils.UserManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_orders);
        btnRefresh = findViewById(R.id.btn_refresh_orders);
        rvOrders = findViewById(R.id.rv_orders);
        layoutOrdersState = findViewById(R.id.layout_orders_state);
        tvOrdersStateTitle = findViewById(R.id.tv_orders_state_title);
        tvOrdersStateMessage = findViewById(R.id.tv_orders_state_message);
        btnRetryOrders = findViewById(R.id.btn_retry_orders);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupRecyclerView() {
        String phone = UserManager.getInstance(this).getUserPhone();
        showState("Đang tải đơn hàng", "Vui lòng chờ trong giây lát.", false);
        com.example.appnhathuoc.api.ApiClient.fetchOrders(phone, new com.example.appnhathuoc.api.ApiClient.ApiCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (orders != null && !orders.isEmpty()) {
                    showOrders(orders);
                } else {
                    showLocalOrders(phone);
                }
            }

            @Override
            public void onError(String errorMessage) {
                List<Order> localOrders = getLocalOrders(phone);
                if (!localOrders.isEmpty()) {
                    showOrders(localOrders);
                    android.widget.Toast.makeText(OrdersActivity.this, errorMessage, android.widget.Toast.LENGTH_LONG).show();
                } else {
                    showState("Chưa tải được đơn hàng", errorMessage, true);
                }
            }
        });
    }

    private void showLocalOrders(String phone) {
        List<Order> orders = getLocalOrders(phone);
        if (orders.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            showState("Bạn chưa có đơn hàng nào", "Các đơn hàng đã đặt bằng số điện thoại tài khoản sẽ xuất hiện tại đây.", false);
            return;
        }
        showOrders(orders);
    }

    private List<Order> getLocalOrders(String phone) {
        List<Order> orders = new java.util.ArrayList<>();
        for (Order order : orderManager.getSavedOrders()) {
            if (phone != null && normalizePhone(phone).equals(normalizePhone(order.getPhone()))) {
                orders.add(order);
            }
        }
        return orders;
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", "");
        return digits.startsWith("84") && digits.length() == 11
                ? "0" + digits.substring(2)
                : digits;
    }

    private void showOrders(List<Order> orders) {
        layoutOrdersState.setVisibility(View.GONE);
        rvOrders.setVisibility(View.VISIBLE);
        adapter = new OrderAdapter(orders, order -> showOrderDetailDialog(order));
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void showState(String title, String message, boolean showRetry) {
        rvOrders.setVisibility(View.GONE);
        layoutOrdersState.setVisibility(View.VISIBLE);
        tvOrdersStateTitle.setText(title);
        tvOrdersStateMessage.setText(message);
        btnRetryOrders.setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> setupRecyclerView());
        btnRetryOrders.setOnClickListener(v -> setupRecyclerView());
    }

    private void showOrderDetailDialog(Order order) {
        View detailView = getLayoutInflater().inflate(R.layout.dialog_order_detail, null);
        TextView tvOrderCode = detailView.findViewById(R.id.tv_detail_order_code);
        TextView tvDate = detailView.findViewById(R.id.tv_detail_date);
        TextView tvStatus = detailView.findViewById(R.id.tv_detail_status);
        TextView tvReceiver = detailView.findViewById(R.id.tv_detail_receiver);
        TextView tvPhone = detailView.findViewById(R.id.tv_detail_phone);
        TextView tvAddress = detailView.findViewById(R.id.tv_detail_address);
        TextView tvPayment = detailView.findViewById(R.id.tv_detail_payment);
        TextView tvTotal = detailView.findViewById(R.id.tv_detail_total);
        LinearLayout layoutItems = detailView.findViewById(R.id.layout_detail_items);

        tvOrderCode.setText("Mã đơn hàng  •  " + valueOrFallback(order.getOrderId(), "Đang cập nhật"));
        tvDate.setText("Ngày đặt: " + valueOrFallback(order.getDate(), "Đang cập nhật"));
        tvStatus.setText(valueOrFallback(order.getStatus(), "Đang xử lý"));
        tvReceiver.setText(valueOrFallback(order.getFullName(), "Khách hàng"));
        tvPhone.setText(valueOrFallback(order.getPhone(), "Chưa có số điện thoại"));
        tvAddress.setText(valueOrFallback(order.getAddress(), "Chưa có địa chỉ giao hàng"));
        tvPayment.setText("Phương thức thanh toán: " + valueOrFallback(order.getPaymentMethod(), "Chưa cập nhật"));
        tvTotal.setText(priceFormatter.format(order.getTotalAmount()));

        LayoutInflater inflater = LayoutInflater.from(this);
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    View itemView = inflater.inflate(R.layout.item_order_detail, layoutItems, false);
                    TextView name = itemView.findViewById(R.id.tv_detail_item_name);
                    TextView quantity = itemView.findViewById(R.id.tv_detail_item_quantity);
                    TextView price = itemView.findViewById(R.id.tv_detail_item_price);
                    name.setText(item.getProduct().getName());
                    quantity.setText("Số lượng: " + item.getQuantity());
                    price.setText(priceFormatter.format(item.getTotalPrice()));
                    layoutItems.addView(itemView);

                    View divider = new View(this);
                    divider.setBackgroundColor(getColor(R.color.divider));
                    layoutItems.addView(divider, new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().density)));
                }
            }
        }

        new AlertDialog.Builder(this)
                .setView(detailView)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void setupBottomNavigation() {
        BottomNavigationHelper.setup(this, bottomNavigationView, R.id.navigation_orders);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupRecyclerView();
        bottomNavigationView.setSelectedItemId(R.id.navigation_orders);
    }
}