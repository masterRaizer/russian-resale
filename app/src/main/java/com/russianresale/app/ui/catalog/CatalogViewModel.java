package com.russianresale.app.ui.catalog;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.russianresale.app.App;
import com.russianresale.app.data.model.ListingFilter;
import com.russianresale.app.data.model.ListingItem;

import java.util.List;

/** Состояние каталога: текущий фильтр и найденные объявления (переживает поворот экрана). */
public class CatalogViewModel extends AndroidViewModel {
    private final MutableLiveData<ListingFilter> filter = new MutableLiveData<>(new ListingFilter());
    private final LiveData<List<ListingItem>> results;

    public CatalogViewModel(@NonNull Application application) {
        super(application);
        App app = (App) application;
        long userId = app.session().getUserId();
        results = Transformations.switchMap(filter, f -> app.listings().search(f, userId));
    }

    public LiveData<ListingFilter> getFilter() {
        return filter;
    }

    public LiveData<List<ListingItem>> getResults() {
        return results;
    }

    /** Текущий фильтр (копия — чтобы изменения применялись только через {@link #apply}). */
    public ListingFilter current() {
        ListingFilter f = filter.getValue();
        return f == null ? new ListingFilter() : f.copy();
    }

    public void apply(ListingFilter f) {
        filter.setValue(f);
    }
}
