package com.github.cattv.osc.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.cattv.osc.R;

public class SearchRecordAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public SearchRecordAdapter() {
        super(R.layout.item_search_record);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tvSearchWord, item);
    }
}
