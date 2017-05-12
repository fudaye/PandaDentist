package com.pandadentist.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseFragment;
import com.pandadentist.util.DensityUtil;
import com.bumptech.glide.Glide;
import com.jph.takephoto.model.TImage;
import com.jph.takephoto.model.TResult;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import me.shaohui.bottomdialog.BottomDialog;

public class TodayFragment extends SwipeRefreshBaseFragment {

    @Bind(R.id.rv)
    RecyclerView mRv;

    MyAdapter myAdapter;
    ArrayList<TImage> mData = new ArrayList<>();

    @Override
    public void createView(ViewGroup container, View parentView, Bundle savedInstanceState) {
        assert mToolBarTtitle != null;
        assert mToolBackRl != null;
        mToolBarTtitle.setText("今日分析");
        mToolBackRl.setVisibility(View.GONE);
        mRv.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        myAdapter = new MyAdapter(mData);
        mRv.setAdapter(myAdapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.test_main;
    }


    @OnClick(R.id.btn)
    public void onClick() {
        BottomDialog bottomDialog = BottomDialog.create(getFragmentManager());
        bottomDialog.setViewListener(new BottomDialog.ViewListener() {
            @Override
            public void bindView(View v) {
                v.findViewById(R.id.btn_take_photo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomDialog.dismiss();
                        takePhoto();
                    }
                });
                v.findViewById(R.id.btn_album).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomDialog.dismiss();
                        takeAlbum();
                    }
                });
            }
        })
                .setLayoutRes(R.layout.dialog_photo_select)
                .show();
    }

    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        ArrayList<TImage> arrayList = result.getImages();
        mData.addAll(arrayList);
        myAdapter.notifyDataSetChanged();
    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        ArrayList<TImage> data;

        public MyAdapter(ArrayList<TImage> data) {
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setLayoutParams(new RecyclerView.LayoutParams((int) DensityUtil.dp(80), (int) DensityUtil.dp(80)));
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Glide.with(getActivity()).load(data.get(position).getPath()).centerCrop().into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }
}
