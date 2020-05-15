package com.china.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.china.R;
import com.china.common.ColumnData;
import com.china.common.MyApplication;
import com.china.utils.CommonUtil;
import com.china.utils.OkHttpUtil;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 主界面
 */
public class ShawnMainAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private List<ColumnData> mArrayList;
	public int height = 0;
	private Context mContext;
	private Handler mUIHandler = new Handler();
	
	private final class ViewHolder{
		TextView tvName;
		ImageView icon;
	}
	
	public ShawnMainAdapter(Context context, List<ColumnData> mArrayList) {
		this.mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.shawn_adapter_main, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.icon = convertView.findViewById(R.id.icon);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		try {
			ColumnData dto = mArrayList.get(position);

			if (!TextUtils.isEmpty(dto.name)) {
				mHolder.tvName.setText(dto.name);
			}

			if (!TextUtils.isEmpty(dto.icon)) {
				if (TextUtils.equals("1", MyApplication.getAppTheme())) {
					downloadBitmap(dto.icon, mHolder.icon);
				} else {
					Picasso.get().load(dto.icon).error(R.drawable.shawn_icon_seat_bitmap).into(mHolder.icon);
				}
			}else {
				if (TextUtils.equals("1", MyApplication.getAppTheme())) {
					mHolder.icon.setImageBitmap(CommonUtil.grayScaleImage(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.shawn_icon_seat_bitmap)));
				} else {
					mHolder.icon.setImageResource(R.drawable.shawn_icon_seat_bitmap);
				}
			}

			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			params.height = height/3;
			convertView.setLayoutParams(params);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return convertView;
	}

	/**
	 * 下载头像保存在本地
	 */
	private void downloadBitmap(final String imgUrl, final ImageView icon) {
		if (TextUtils.isEmpty(imgUrl)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(imgUrl).build(), new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
					}
					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
						final byte[] bytes = response.body().bytes();
						mUIHandler.post(new Runnable() {
							@Override
							public void run() {
								Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
								icon.setImageBitmap(CommonUtil.grayScaleImage(bitmap));
							}
						});
					}
				});
			}
		}).start();
	}

}
