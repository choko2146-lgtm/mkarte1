package com.example.mkarte1;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mkarte1.data.Photo;
import com.example.mkarte1.repository.PhotoRepository;
import com.example.mkarte1.ui.photo.PhotoDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhotoListActivity extends AppCompatActivity {
    private static final String[] SORT_OPTIONS = {
            "撮影日 新しい順",
            "撮影日 古い順",
            "顧客名 昇順",
            "顧客名 降順"
    };

    private PhotoRepository photoRepository;
    private RecyclerView recyclerPhotos;
    private TextView textNoPhotos;
    private EditText editPhotoSearch;
    private Spinner spinnerPhotoSort;
    private PhotoListAdapter adapter;
    private final List<Photo> allPhotos = new ArrayList<>();
    private boolean photosLoaded;
    private int selectedSortIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("写真一覧");
        setContentView(R.layout.activity_photo_list);

        photoRepository = new PhotoRepository(this);
        recyclerPhotos = findViewById(R.id.recyclerPhotos);
        textNoPhotos = findViewById(R.id.textNoPhotos);
        editPhotoSearch = findViewById(R.id.editPhotoSearch);
        spinnerPhotoSort = findViewById(R.id.spinnerPhotoSort);

        adapter = new PhotoListAdapter(this::openPhotoDetail);
        recyclerPhotos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPhotos.setAdapter(adapter);
        setupSearch();
        setupSort();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPhotos();
    }

    private void loadPhotos() {
        photoRepository.listAll(photos -> {
            allPhotos.clear();
            if (photos != null) {
                allPhotos.addAll(photos);
            }
            photosLoaded = true;
            applyFiltersAndSort();
        });
    }

    private void setupSearch() {
        editPhotoSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSort() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                SORT_OPTIONS
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhotoSort.setAdapter(sortAdapter);
        spinnerPhotoSort.setSelection(0);
        spinnerPhotoSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSortIndex = position;
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void applyFiltersAndSort() {
        if (!photosLoaded) {
            adapter.submit(new ArrayList<>());
            recyclerPhotos.setVisibility(View.GONE);
            textNoPhotos.setVisibility(View.GONE);
            return;
        }

        String query = normalize(editPhotoSearch.getText().toString());
        List<Photo> filteredPhotos = new ArrayList<>();
        for (Photo photo : allPhotos) {
            if (matchesQuery(photo, query)) {
                filteredPhotos.add(photo);
            }
        }

        sortPhotos(filteredPhotos);
        adapter.submit(filteredPhotos);

        boolean hasPhotos = !filteredPhotos.isEmpty();
        recyclerPhotos.setVisibility(hasPhotos ? View.VISIBLE : View.GONE);
        textNoPhotos.setText(allPhotos.isEmpty() ? "写真がありません" : "該当する写真がありません");
        textNoPhotos.setVisibility(hasPhotos ? View.GONE : View.VISIBLE);
    }

    private boolean matchesQuery(Photo photo, String query) {
        if (query.isEmpty()) {
            return true;
        }
        if (photo == null) {
            return false;
        }
        return normalize(photo.customerName).contains(query) || normalize(photo.memo).contains(query);
    }

    private void sortPhotos(List<Photo> photos) {
        switch (selectedSortIndex) {
            case 1:
                photos.sort((left, right) -> compareTakenDate(left, right, false));
                break;
            case 2:
                photos.sort((left, right) -> compareCustomerName(left, right, false));
                break;
            case 3:
                photos.sort((left, right) -> compareCustomerName(left, right, true));
                break;
            case 0:
            default:
                photos.sort((left, right) -> compareTakenDate(left, right, true));
                break;
        }
    }

    private int compareTakenDate(Photo left, Photo right, boolean newestFirst) {
        String leftDate = safe(left == null ? null : left.takenDate);
        String rightDate = safe(right == null ? null : right.takenDate);
        boolean leftEmpty = leftDate.isEmpty();
        boolean rightEmpty = rightDate.isEmpty();
        if (leftEmpty && !rightEmpty) {
            return 1;
        }
        if (!leftEmpty && rightEmpty) {
            return -1;
        }

        int result = leftDate.compareTo(rightDate);
        if (newestFirst) {
            result = -result;
        }
        if (result != 0) {
            return result;
        }

        int createdAtResult = Long.compare(
                left == null ? 0 : left.createdAt,
                right == null ? 0 : right.createdAt
        );
        return newestFirst ? -createdAtResult : createdAtResult;
    }

    private int compareCustomerName(Photo left, Photo right, boolean descending) {
        int result = normalize(left == null ? null : left.customerName)
                .compareTo(normalize(right == null ? null : right.customerName));
        if (descending) {
            result = -result;
        }
        if (result != 0) {
            return result;
        }
        return compareTakenDate(left, right, true);
    }

    private String normalize(String value) {
        return safe(value).toLowerCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void openPhotoDetail(Photo photo) {
        if (photo == null || photo.id <= 0) {
            Toast.makeText(this, "写真情報を開けません", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PhotoDetailActivity.class);
        intent.putExtra("photoId", photo.id);
        startActivity(intent);
    }
}
