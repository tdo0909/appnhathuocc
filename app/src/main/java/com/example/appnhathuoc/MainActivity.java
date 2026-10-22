package com.example.appnhathuoc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appnhathuoc.adapter.BannerAdapter;
import com.example.appnhathuoc.adapter.CategoryAdapter;
import com.example.appnhathuoc.adapter.ProductAdapter;
import com.example.appnhathuoc.api.ApiClient;
import com.example.appnhathuoc.data.DataRepository;
import com.example.appnhathuoc.model.Article;
import com.example.appnhathuoc.model.BannerItem;
import com.example.appnhathuoc.model.Category;
import com.example.appnhathuoc.model.Product;
import com.example.appnhathuoc.utils.CartManager;
import com.example.appnhathuoc.utils.BottomNavigationHelper;
import com.example.appnhathuoc.utils.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navViewDrawer;
    private ImageView btnHamburgerMenu;
    private ViewPager2 viewPagerBanner;
    private RecyclerView rvCategories, rvBestSellers, rvPromotions, rvHomeActivities, rvHomeArticles;
    private LinearLayout homeCategoryProductsContainer;
    private TextView tvCartBadge, tvSeeAllCategories, tvSeeAllBestSellers, tvSeeAllPromotions, tvSeeAllActivities, tvSeeAllHealth, tvHeaderUserName, tvHomeActivitiesEmpty;
    private TextView tvDrawerUserName, tvDrawerUserPhone;
    private FrameLayout btnCartHeader;
    private LinearLayout layoutSearchBar, btnHeaderUserStatus;
    private BottomNavigationView bottomNavigationView;

    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private CartManager cartManager;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        cartManager = CartManager.getInstance(this);
        userManager = UserManager.getInstance(this);

        initViews();
        setupNavigationDrawer();
        setupBottomNavigation();
        setupBanner();
        setupCategories();
        setupBestSellers();
        setupPromotions();
        setupHealthArticles();
        setupListeners();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navViewDrawer = findViewById(R.id.nav_view_drawer);
        btnHamburgerMenu = findViewById(R.id.btn_hamburger_menu);
        viewPagerBanner = findViewById(R.id.view_pager_banner);
        rvCategories = findViewById(R.id.rv_home_categories);
        rvBestSellers = findViewById(R.id.rv_best_sellers);
        rvPromotions = findViewById(R.id.rv_promotions);
        rvHomeActivities = findViewById(R.id.rv_home_activities);
        rvHomeArticles = findViewById(R.id.rv_home_articles);
        homeCategoryProductsContainer = findViewById(R.id.home_category_products_container);
        tvCartBadge = findViewById(R.id.tv_cart_badge);
        tvSeeAllCategories = findViewById(R.id.tv_see_all_categories);
        tvSeeAllBestSellers = findViewById(R.id.tv_see_all_best_sellers);
        tvSeeAllPromotions = findViewById(R.id.tv_see_all_promotions);
        tvSeeAllActivities = findViewById(R.id.tv_see_all_activities);
        tvSeeAllHealth = findViewById(R.id.tv_see_all_health);
        tvHomeActivitiesEmpty = findViewById(R.id.tv_home_activities_empty);
        tvHeaderUserName = findViewById(R.id.tv_header_user_name);
        btnHeaderUserStatus = findViewById(R.id.btn_header_user_status);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        btnCartHeader = findViewById(R.id.btn_cart_header);
        layoutSearchBar = findViewById(R.id.layout_search_bar);

        if (navViewDrawer != null && navViewDrawer.getHeaderCount() > 0) {
            View headerView = navViewDrawer.getHeaderView(0);
            tvDrawerUserName = headerView.findViewById(R.id.tv_drawer_user_name);
            tvDrawerUserPhone = headerView.findViewById(R.id.tv_drawer_user_phone);

            headerView.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                if (userManager.isLoggedIn()) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
        }
    }

    private void setupNavigationDrawer() {
        btnHamburgerMenu.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navViewDrawer.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);

            if (id == R.id.nav_drawer_home) {
                return true;
            } else if (id == R.id.nav_drawer_category) {
                startActivity(new Intent(MainActivity.this, CategoryActivity.class));
                return true;
            } else if (id == R.id.nav_drawer_health_plan) {
                startActivity(new Intent(MainActivity.this, HealthPlanActivity.class));
                return true;
            } else if (id == R.id.nav_drawer_consult) {
                showPharmacistChatConversationDialog();
                return true;
            } else if (id == R.id.nav_drawer_store) {
                startActivity(new Intent(MainActivity.this, StoreActivity.class));
                return true;
            } else if (id == R.id.nav_drawer_orders) {
                startActivity(new Intent(MainActivity.this, OrdersActivity.class));
                return true;
            } else if (id == R.id.nav_drawer_profile) {
                if (userManager.isLoggedIn()) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                return true;
            } else if (id == R.id.nav_drawer_notif) {
                startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                return true;
            }
            return false;
        });
    }

    private void showPharmacistChatConversationDialog() {
        View chatView = getLayoutInflater().inflate(R.layout.dialog_pharmacist_chat, null);
        LinearLayout layoutMessages = chatView.findViewById(R.id.layout_chat_messages);
        android.widget.ScrollView scrollChat = chatView.findViewById(R.id.scroll_chat_history);
        android.widget.EditText etInput = chatView.findViewById(R.id.et_chat_input);
        Button btnSend = chatView.findViewById(R.id.btn_send_chat);
        List<com.example.appnhathuoc.api.GeminiApiClient.ChatMessage> conversation = new ArrayList<>();

        String greeting = "👋 Xin chào! Mình là trợ lý AI sức khỏe của nhà thuốc, không thay thế dược sĩ/bác sĩ. Mình có thể cung cấp thông tin tham khảo hoặc giúp ghi lại chỉ định bạn đã nhận. Bạn cần hỗ trợ gì?";
        addChatMessage(layoutMessages, scrollChat, greeting, false);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(chatView)
                .setNegativeButton("Đóng trò chuyện", null)
                .setNeutralButton("📞 Gọi Hotline 1800 6928", (d, w) -> callHotline())
                .create();

        btnSend.setOnClickListener(v -> {
            String userText = etInput.getText().toString().trim();
            if (!userText.isEmpty()) {
                etInput.setText("");
                sendPharmacistMessage(userText, layoutMessages, scrollChat, etInput, btnSend, conversation);
            }
        });

        View.OnClickListener chipListener = v -> {
            TextView tvChip = (TextView) v;
            String text = tvChip.getText().toString();
            sendPharmacistMessage(text, layoutMessages, scrollChat, etInput, btnSend, conversation);
        };

        TextView chipSot = chatView.findViewById(R.id.chip_chat_sot);
        TextView chipPara = chatView.findViewById(R.id.chip_chat_paracetamol);
        TextView chipDaday = chatView.findViewById(R.id.chip_chat_daday);
        TextView chipVitC = chatView.findViewById(R.id.chip_chat_vitaminc);

        if (chipSot != null) chipSot.setOnClickListener(chipListener);
        if (chipPara != null) chipPara.setOnClickListener(chipListener);
        if (chipDaday != null) chipDaday.setOnClickListener(chipListener);
        if (chipVitC != null) chipVitC.setOnClickListener(chipListener);

        dialog.show();
    }

    private void sendPharmacistMessage(
            String userText,
            LinearLayout layoutMessages,
            android.widget.ScrollView scrollChat,
            android.widget.EditText etInput,
            Button btnSend,
            List<com.example.appnhathuoc.api.GeminiApiClient.ChatMessage> conversation) {
        if (userText == null || userText.trim().isEmpty() || !btnSend.isEnabled()) return;

        conversation.add(new com.example.appnhathuoc.api.GeminiApiClient.ChatMessage("user", userText));
        addChatMessage(layoutMessages, scrollChat, userText, true);
        btnSend.setEnabled(false);
        etInput.setEnabled(false);
        TextView typingIndicator = addChatMessage(layoutMessages, scrollChat, "Trợ lý AI đang soạn phản hồi...", false);

        com.example.appnhathuoc.api.GeminiApiClient.generateAiResponse(new ArrayList<>(conversation),
                new com.example.appnhathuoc.api.GeminiApiClient.GeminiCallback() {
                    @Override
                    public void onSuccess(String aiResponse) {
                        layoutMessages.removeView(typingIndicator);
                        conversation.add(new com.example.appnhathuoc.api.GeminiApiClient.ChatMessage("model", aiResponse));
                        addChatMessage(layoutMessages, scrollChat, aiResponse, false);
                        btnSend.setEnabled(true);
                        etInput.setEnabled(true);
                    }

                    @Override
                    public void onError(String error) {
                        layoutMessages.removeView(typingIndicator);
                        addChatMessage(layoutMessages, scrollChat,
                                "Mình chưa nhận được phản hồi từ Gemini. " + error +
                                        "\n\nBạn có thể thử lại hoặc gọi hotline 1800 6928 nhé.",
                                false);
                        btnSend.setEnabled(true);
                        etInput.setEnabled(true);
                    }
                });
    }

    private TextView addChatMessage(LinearLayout container, android.widget.ScrollView scroll, String text, boolean isUser) {
        TextView tvMsg = new TextView(this);
        tvMsg.setText(text);
        tvMsg.setTextSize(13.5f);
        tvMsg.setPadding(28, 20, 28, 20);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 12;
        params.bottomMargin = 4;

        if (isUser) {
            params.gravity = android.view.Gravity.END;
            tvMsg.setBackgroundResource(R.drawable.bg_button_primary);
            tvMsg.setTextColor(getResources().getColor(R.color.white));
        } else {
            params.gravity = android.view.Gravity.START;
            tvMsg.setBackgroundResource(R.drawable.bg_category_icon);
            tvMsg.setTextColor(getResources().getColor(R.color.text_primary));
        }

        tvMsg.setLayoutParams(params);
        container.addView(tvMsg);

        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        return tvMsg;
    }

    private void callHotline() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:18006928"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Tư vấn Dược sĩ Hotline: 1800 6928 (Miễn phí)", Toast.LENGTH_LONG).show();
        }
    }

    private void setupBanner() {
        DataRepository.loadBannersFromApi(new ApiClient.ApiCallback<List<BannerItem>>() {
            @Override
            public void onSuccess(List<BannerItem> banners) {
                if (banners == null || banners.isEmpty()) return;
                BannerAdapter adapter = new BannerAdapter(banners, banner -> {
                    Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                    intent.putExtra("TITLE", banner.getTitle());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
                viewPagerBanner.setAdapter(adapter);
                viewPagerBanner.setPageTransformer(new com.example.appnhathuoc.utils.ZoomOutPageTransformer());

                if (bannerRunnable != null) {
                    bannerHandler.removeCallbacks(bannerRunnable);
                }

                bannerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        int current = viewPagerBanner.getCurrentItem();
                        int total = adapter.getItemCount();
                        if (total > 0) {
                            viewPagerBanner.setCurrentItem((current + 1) % total, true);
                        }
                        bannerHandler.postDelayed(this, 3500);
                    }
                };
                bannerHandler.postDelayed(bannerRunnable, 3500);
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationHelper.setup(this, bottomNavigationView, R.id.navigation_home);
    }

    private void setupBestSellers() {
        DataRepository.loadProductsFromApi(null, true, false, null, null, new ApiClient.ApiCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products == null) return;
                ProductAdapter adapter = new ProductAdapter(products, new ProductAdapter.OnProductClickListener() {
                    @Override
                    public void onProductClick(Product product) {
                        openProductDetail(product);
                    }

                    @Override
                    public void onAddToCartClick(Product product) {
                        cartManager.addProduct(product, 1);
                        updateCartBadge();
                        Toast.makeText(MainActivity.this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                });
                rvBestSellers.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                rvBestSellers.setAdapter(adapter);
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void setupCategories() {
        DataRepository.loadCategoriesFromApi(new ApiClient.ApiCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (categories == null || categories.isEmpty()) return;
                CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                    Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                    intent.putExtra("CATEGORY_ID", category.getId());
                    intent.putExtra("CATEGORY_NAME", category.getName());
                    startActivity(intent);
                });
                rvCategories.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                rvCategories.setAdapter(adapter);
                setupHomeCategoryProducts(categories);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Không tải được danh mục: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHomeCategoryProducts(List<Category> categories) {
        DataRepository.loadProductsFromApi(null, false, false, null, null, new ApiClient.ApiCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products == null || products.isEmpty()) return;

                for (Category category : categories) {
                    if (category == null || category.getId() == null || category.getId().trim().isEmpty()) continue;

                    List<Product> categoryProducts = new ArrayList<>();
                    for (Product product : products) {
                        if (product != null && category.getId().equalsIgnoreCase(product.getCategoryId())) {
                            categoryProducts.add(product);
                        }
                    }
                    if (categoryProducts.isEmpty()) continue;

                    View section = getLayoutInflater().inflate(R.layout.item_home_category_section, homeCategoryProductsContainer, false);
                    TextView title = section.findViewById(R.id.tv_home_category_title);
                    TextView seeAll = section.findViewById(R.id.tv_home_category_see_all);
                    RecyclerView recyclerView = section.findViewById(R.id.rv_home_category_products);
                    title.setText(category.getName());
                    seeAll.setOnClickListener(v -> openCategoryProducts(category));
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    recyclerView.setAdapter(new ProductAdapter(categoryProducts, new ProductAdapter.OnProductClickListener() {
                        @Override
                        public void onProductClick(Product product) {
                            openProductDetail(product);
                        }

                        @Override
                        public void onAddToCartClick(Product product) {
                            cartManager.addProduct(product, 1);
                            updateCartBadge();
                            Toast.makeText(MainActivity.this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    }));
                    homeCategoryProductsContainer.addView(section);
                }
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void openCategoryProducts(Category category) {
        Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
        intent.putExtra("CATEGORY_ID", category.getId());
        intent.putExtra("CATEGORY_NAME", category.getName());
        startActivity(intent);
    }

    private void setupPromotions() {
        DataRepository.loadProductsFromApi(null, false, true, null, null, new ApiClient.ApiCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products == null) return;
                ProductAdapter adapter = new ProductAdapter(products, new ProductAdapter.OnProductClickListener() {
                    @Override
                    public void onProductClick(Product product) {
                        openProductDetail(product);
                    }

                    @Override
                    public void onAddToCartClick(Product product) {
                        cartManager.addProduct(product, 1);
                        updateCartBadge();
                        Toast.makeText(MainActivity.this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                });
                rvPromotions.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                rvPromotions.setAdapter(adapter);
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void setupHealthArticles() {
        DataRepository.loadArticlesFromApi(new ApiClient.ApiCallback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> articlesList) {
                if (articlesList == null || articlesList.isEmpty()) return;
                List<Article> displayList = articlesList.size() > 3 ? articlesList.subList(0, 3) : articlesList;
                com.example.appnhathuoc.adapter.ArticleAdapter adapter = new com.example.appnhathuoc.adapter.ArticleAdapter(displayList, article -> {
                    Intent intent = new Intent(MainActivity.this, ArticleDetailActivity.class);
                    intent.putExtra("ARTICLE", article);
                    startActivity(intent);
                });
                rvHomeArticles.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                rvHomeArticles.setAdapter(adapter);
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    private void setupHomeActivities() {
        ApiClient.fetchNotifications(new ApiClient.ApiCallback<List<com.example.appnhathuoc.model.NotificationItem>>() {
            @Override
            public void onSuccess(List<com.example.appnhathuoc.model.NotificationItem> notifications) {
                List<com.example.appnhathuoc.model.NotificationItem> activities = new ArrayList<>();
                if (notifications != null) {
                    for (com.example.appnhathuoc.model.NotificationItem notification : notifications) {
                        if (notification != null && "PROMOTION".equalsIgnoreCase(notification.getType())) {
                            activities.add(notification);
                        }
                    }
                }

                if (activities.size() > 3) {
                    activities = new ArrayList<>(activities.subList(0, 3));
                }
                tvHomeActivitiesEmpty.setVisibility(activities.isEmpty() ? View.VISIBLE : View.GONE);
                rvHomeActivities.setVisibility(activities.isEmpty() ? View.GONE : View.VISIBLE);
                if (!activities.isEmpty()) {
                    com.example.appnhathuoc.adapter.NotificationAdapter adapter =
                            new com.example.appnhathuoc.adapter.NotificationAdapter(activities,
                                    notification -> startActivity(new Intent(MainActivity.this, NotificationActivity.class)));
                    rvHomeActivities.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    rvHomeActivities.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String errorMessage) {
                tvHomeActivitiesEmpty.setVisibility(View.VISIBLE);
                rvHomeActivities.setVisibility(View.GONE);
            }
        });
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT", product);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void setupListeners() {
        View btnMascot = findViewById(R.id.btn_floating_mascot);
        if (btnMascot != null) {
            btnMascot.setOnClickListener(v -> showPharmacistChatConversationDialog());
        }



        View btnActionHealthPlan = findViewById(R.id.btn_action_health_plan);
        if (btnActionHealthPlan != null) {
            btnActionHealthPlan.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HealthPlanActivity.class)));
        }

        View btnBuyMedicine = findViewById(R.id.btn_action_buy_medicine);
        if (btnBuyMedicine != null) {
            btnBuyMedicine.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        }

        View btnActionConsult = findViewById(R.id.btn_action_consult);
        if (btnActionConsult != null) {
            btnActionConsult.setOnClickListener(v -> showPharmacistChatConversationDialog());
        }

        if (tvSeeAllCategories != null) {
            tvSeeAllCategories.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CategoryActivity.class)));
        }

        View btnMyOrders = findViewById(R.id.btn_action_my_orders);
        if (btnMyOrders != null) {
            btnMyOrders.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OrdersActivity.class)));
        }

        View btnFindStore = findViewById(R.id.btn_action_find_store);
        if (btnFindStore != null) {
            btnFindStore.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, StoreActivity.class)));
        }

        View btnVaccine = findViewById(R.id.btn_action_vaccine);
        if (btnVaccine != null) {
            btnVaccine.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("💉 Dịch Vụ Tiêm Vắc Xin Nhà Thuốc")
                        .setMessage("Hệ thống nhà thuốc cung cấp đầy đủ Vắc xin Cúm, Phế cầu, Tetanus... Hãy liên hệ Tổng đài Dược sĩ 1800 6928 để đăng ký lịch tiêm.")
                        .setPositiveButton("Gọi Hotline 1800 6928", (dialog, which) -> callHotline())
                        .setNegativeButton("Đóng", null)
                        .show();
            });
        }

        if (btnHeaderUserStatus != null) {
            btnHeaderUserStatus.setOnClickListener(v -> {
                if (userManager.isLoggedIn()) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
        }

        layoutSearchBar.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));

        if (btnCartHeader != null) {
            btnCartHeader.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CartActivity.class)));
        }

        if (tvSeeAllBestSellers != null) {
            tvSeeAllBestSellers.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                intent.putExtra("TITLE", "Sản phẩm bán chạy");
                startActivity(intent);
            });
        }

        if (tvSeeAllPromotions != null) {
            tvSeeAllPromotions.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                intent.putExtra("TITLE", "Ưu đãi hấp dẫn");
                startActivity(intent);
            });
        }

        if (tvSeeAllActivities != null) {
            tvSeeAllActivities.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, NotificationActivity.class)));
        }

        if (tvSeeAllHealth != null) {
            tvSeeAllHealth.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HealthActivity.class)));
        }
    }

    private void updateCartBadge() {
        int count = cartManager.getCartCount();
        if (count > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(String.valueOf(count));
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void updateUserHeader() {
        if (userManager != null) {
            if (userManager.isLoggedIn()) {
                if (tvHeaderUserName != null) tvHeaderUserName.setText("👋 Xin chào, " + userManager.getUserName());
                if (tvDrawerUserName != null) tvDrawerUserName.setText(userManager.getUserName());
                if (tvDrawerUserPhone != null) tvDrawerUserPhone.setText(userManager.getUserPhone().isEmpty() ? "Đã đăng nhập" : userManager.getUserPhone());
            } else {
                if (tvHeaderUserName != null) tvHeaderUserName.setText("👋 Đăng nhập / Đăng ký");
                if (tvDrawerUserName != null) tvDrawerUserName.setText("Khách Hàng Thân Thiết");
                if (tvDrawerUserPhone != null) tvDrawerUserPhone.setText("👉 Nhấn vào đây để đăng nhập / đăng ký");
            }
        }
    }

    private final DataRepository.OnDataUpdatedListener realtimeDataListener = this::refreshDataFromCloud;

    private void refreshDataFromCloud() {
        setupBestSellers();
        setupPromotions();
        setupHealthArticles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserHeader();
        updateCartBadge();
        refreshDataFromCloud();
        setupHomeActivities();
        DataRepository.addDataUpdatedListener(realtimeDataListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DataRepository.removeDataUpdatedListener(realtimeDataListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataRepository.removeDataUpdatedListener(realtimeDataListener);
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}