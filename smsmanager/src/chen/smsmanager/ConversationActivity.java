package chen.smsmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract.PhoneLookup;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import chen.smsother.Groups;
import chen.smsother.Sms;
import chen.smsother.Thread_Groups;

public class ConversationActivity extends Activity implements OnClickListener{
	
	private Button bt_new_msg;
	private Button bt_all_select;
	private Button bt_cancel_selected;
	private Button bt_delete;
	
	private LinearLayout mEdit;
	private ListView mListView;
	private TextView tv_empty;
	
	private QueryHandler mQueryHandler;
	private ConversationAdapter mAdapter;
	
	//���ŻỰ��ͶӰ
	private final static String[] CONVERSATION_PROJECTION = new String[]{"sms.thread_id as _id",
		"snippet",
		"msg_count",
		"sms.address as address",
		"sms.date as date"};
	
	private final static int ID_COLUMN_INDEX = 0;
	private final static int SNIPPET_COLUMN_INDEX = 1;
	private final static int MSG_COUNT_COLUMN_INDEX = 2;
	private final static int ADDRESS_COLUMN_INDEX = 3;
	private final static int DATE_COLUMN_INDEX = 4;
	
	//��ϵ�˵�ͶӰ
	private final static String[] CONTACT_PROJECTION = new String[]{PhoneLookup.DISPLAY_NAME};
	private final static int DISPLAY_NAME_COLUMN_INDEX = 0;
	
	
	//Ⱥ���ͶӰ
	private final static String[] GROUP_PROJECTION = new String[]{Groups._ID,Groups.GROUP_NAME};
	private final static int GROUP_NAME_COLUMN_INDEX = 1;
	
	
	private static final int MENU_SEARCH_ID = Menu.NONE + 1;
	private static final int MENU_DELETE_ID = Menu.NONE + 2;
	private static final int MENU_BACK_ID = Menu.NONE + 3;
	
	private MenuItem menu_item_search;
	private MenuItem menu_item_delete;
	private MenuItem menu_item_back;
	
	//Activity����ʾģʽ
	enum DISPLAYMODE{
		list,edit
	};
	private DISPLAYMODE mode = DISPLAYMODE.list;
	
	private HashSet<String> mMultSelected = new HashSet<String>();
	
	private ProgressDialog mProgressDialog;
	
	private boolean delete = true;
	
	private String thread_ids;
	private String group_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.conversation);
		
		thread_ids = getIntent().getStringExtra("thread_ids");
		group_name = getIntent().getStringExtra("group_name");
		
		initTitle();
		
		initView();
		
		startQuery();
	}

	/**
	 * ��ʼ������
	 */
    private void initTitle() {
		// TODO Auto-generated method stub
		if(group_name != null){
			setTitle(group_name);
		}
	}

	/**
     * ��ѯ���ŵ�����
     * getContentResolver().query();
     * managedQuery(uri, projection, selection, selectionArgs, sortOrder) �����ֶ���ȥ����cursor,��activityȥ�����ǹ���
     * ���ֱ�Ӳ�ѯ�����������߳̽��еġ�
     * ���Բ���android�ṩ���첽���,
     * ʹ�÷�Χ��ֻ��ȥ�������ǵ�ContentProvider���ṩ������
     */
    private void startQuery() {
		// TODO Auto-generated method stub
    	Uri uri = Sms.CONVERSATION_URI;
    	
    	// select * from table where thread_id in (1,2,4)
    	if(thread_ids != null){
    		String where = Sms.THREAD_ID + " in " + thread_ids;
        	mQueryHandler.startQuery(0, null, uri, CONVERSATION_PROJECTION, where, null, " date desc");
    	}else{
        	mQueryHandler.startQuery(0, null, uri, CONVERSATION_PROJECTION, null, null, " date desc");
    	}

		
	}


	/**
     * ��ʼ���ؼ�
     */
	private void initView() {
		// TODO Auto-generated method stub
		bt_new_msg = (Button) findViewById(R.id.bt_new_msg);
		bt_all_select = (Button) findViewById(R.id.bt_all_select);
		bt_cancel_selected = (Button) findViewById(R.id.bt_cancel_selected);
		bt_delete = (Button) findViewById(R.id.bt_delete);
		
		//���õ�������¼�
		bt_new_msg.setOnClickListener(this);
		bt_all_select.setOnClickListener(this);
		bt_cancel_selected.setOnClickListener(this);
		bt_delete.setOnClickListener(this);
		
		mEdit = (LinearLayout) findViewById(R.id.edit);
		
		mListView = (ListView) findViewById(R.id.listview);
		tv_empty = (TextView) findViewById(R.id.empty);
		
		if (thread_ids != null) {
			bt_new_msg.setVisibility(View.GONE);
		}
		
		mEdit.setVisibility(View.GONE);
		bt_delete.setVisibility(View.GONE);
		
		//��listview��������Ϊ�յ�ʱ����ʾһ������
		mListView.setEmptyView(tv_empty);
		
		mQueryHandler = new QueryHandler(getContentResolver());
		
		mAdapter = new ConversationAdapter(this, null);
		
		mListView.setAdapter(mAdapter);
		
		//��listview������Ŀ����¼�
		mListView.setOnItemClickListener(new MyOnItemClickListener());
		
		//����listview�����¼�
		mListView.setOnItemLongClickListener(new MyOnItemLongClickListener());
		
	}
	
	
	private final class MyOnItemLongClickListener implements OnItemLongClickListener{

		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			if(mode == DISPLAYMODE.list){
				//�õ��Ự��id
				Cursor cursor = (Cursor) mAdapter.getItem(position);
				final String thread_id = cursor.getString(ID_COLUMN_INDEX);
				//��ѯȺ������
				final HashMap<String,String> groupsMap = new HashMap<String,String>();
				Uri uri = Groups.CONTENT_URI;
				Cursor group_cursor = getContentResolver().query(uri, GROUP_PROJECTION, null, null, null);
				if(group_cursor.getCount() > 0){
					while(group_cursor.moveToNext()){
						String group_id = group_cursor.getString(ID_COLUMN_INDEX);
						String group_name = group_cursor.getString(GROUP_NAME_COLUMN_INDEX);
						groupsMap.put(group_name, group_id);
					}
					group_cursor.close();
				}else{
					group_cursor.close();
					Toast.makeText(getApplicationContext(), R.string.please_create_group, Toast.LENGTH_LONG).show();
					return true;
				}
				//����Ⱥ������
				int i = 0;
				final String[] groups = new String[groupsMap.size()];
				for(Map.Entry<String, String> entry:groupsMap.entrySet()){
					groups[i] = entry.getKey();
					i++;
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
				
				builder.setTitle(R.string.select_collection_group);
				builder.setItems(groups, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String group_name = groups[which];
						String group_id = groupsMap.get(group_name);
						
						//�ѻỰ�ղص�Ⱥ��
						/**
						 * 1 ���жϻỰ�Ƿ��Ѿ��ղص��˸�Ⱥ��
						 * 
						 */
						Uri uri = Thread_Groups.CONTENT_URI;
						String selection = Thread_Groups.THREAD_ID + " = ? and " + Thread_Groups.GROUP_ID + " = ?";
						String[] selectionArgs = new String[]{thread_id,group_id};
						Cursor exist_cursor = getContentResolver().query(uri, null, selection, selectionArgs, null);
						if(exist_cursor.moveToFirst()){
							Toast.makeText(getApplicationContext(), R.string.exist_collection_group, Toast.LENGTH_SHORT).show();
							exist_cursor.close();
						}else{
							//������
							ContentValues values = new ContentValues();
							values.put(Thread_Groups.THREAD_ID, thread_id);
							values.put(Thread_Groups.GROUP_ID, group_id);
							getContentResolver().insert(uri, values);
							Toast.makeText(getApplicationContext(), R.string.success_collection_group, Toast.LENGTH_SHORT).show();
							exist_cursor.close();
						}
					}
				});
				
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			return true;
		}
		
	}
	
	
	private final class MyOnItemClickListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Cursor cursor = (Cursor) mAdapter.getItem(position);
			String idStr = cursor.getString(ID_COLUMN_INDEX);
			if(mode == DISPLAYMODE.edit){
				ConversationViews views = (ConversationViews) view.getTag();
				CheckBox checkbox = views.checkbox;
				if(mMultSelected.contains(idStr)){
					mMultSelected.remove(idStr);
					checkbox.setChecked(false);
				}else{
					mMultSelected.add(idStr);
					checkbox.setChecked(true);
				}
				
				if(mMultSelected.size() > 0){
					bt_cancel_selected.setEnabled(true);
					bt_delete.setEnabled(true);
				}else{
					bt_cancel_selected.setEnabled(false);
					bt_delete.setEnabled(false);
				}
				
				if(mMultSelected.size() == mAdapter.getCount()){
					bt_all_select.setEnabled(false);
				}else{
					bt_all_select.setEnabled(true);
				}
			}else{
				Intent intent = new Intent(ConversationActivity.this,ConversastionListActivity.class);
				intent.putExtra("thread_id", idStr);
				startActivity(intent);
			}

		}
		
	}
	
	private final class ConversationViews{
		CheckBox checkbox;
		ImageView header;
		TextView tv_name;
		TextView tv_body;
		TextView tv_date;
	}
	
	private final class ConversationAdapter extends CursorAdapter{
		
		private LayoutInflater mInflater;
		private long firstSecondOfToday;//������ʼʱ��ĺ�����

		public ConversationAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
//			mInflater = getLayoutInflater();
//			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mInflater = LayoutInflater.from(context);
			
			Time time = new Time();
			time.setToNow();
			time.hour = 0;
			time.minute = 0;
			time.second = 0;
			//false:��������޸ĵ���time�����ʱ�䣬��ôת��Ϊ����������׼ȷ   true:�����޸���time���������
			firstSecondOfToday = time.toMillis(false);
		}

		/**
		 * ����ListView��item����,ֻ�ᱻ����һ��
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			/**
			 * ���ز��ֵ�ʱ�����ָ��true�����ǰѼ��ؽ����Ĳ�����ӵ���Ԫ������
			 * ���ָ������false,���ǲ���ӵ���Ԫ�����ϣ��������ϣ��ʹ�ò��ֲ��᱾��Ŀ�ߣ�������Ҫ����parent
			 */
			View view = mInflater.inflate(R.layout.conversation_item, parent, false);
			//View view = View.inflate(context, R.layout.conversation_item, parent);
			ConversationViews views = new ConversationViews();
			views.checkbox= (CheckBox) view.findViewById(R.id.checkbox);
			views.header = (ImageView) view.findViewById(R.id.header);
			views.tv_name = (TextView) view.findViewById(R.id.tv_name);
			views.tv_date = (TextView) view.findViewById(R.id.tv_date);
			views.tv_body = (TextView) view.findViewById(R.id.tv_body);
			
			view.setTag(views);
			
			return view;
		}

		/**
		 * ��item������
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			//�õ��ؼ�
/*			δ�Ż�֮ǰ�Ĵ���
 *          CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
			ImageView header = (ImageView) view.findViewById(R.id.header);
			TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
			TextView tv_date = (TextView) view.findViewById(R.id.tv_date);
			TextView tv_body = (TextView) view.findViewById(R.id.tv_body);*/
			
			ConversationViews views = (ConversationViews) view.getTag();
			CheckBox checkBox = views.checkbox;
			ImageView header = views.header;
			TextView tv_name = views.tv_name;
			TextView tv_date = views.tv_date;
			TextView tv_body = views.tv_body;
			
			//�õ�����
			String idStr = cursor.getString(ID_COLUMN_INDEX);
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			int msg_count = cursor.getInt(MSG_COUNT_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(SNIPPET_COLUMN_INDEX);
			
			//���ݵ绰����ȥ��ѯ��ϵ�˵�����
			String name = null;
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
			Cursor contactCursor = getContentResolver().query(uri, CONTACT_PROJECTION, null, null, null);
			if(contactCursor.moveToFirst()){
				name = contactCursor.getString(DISPLAY_NAME_COLUMN_INDEX);
			}
			contactCursor.close();
			
			if(mode == DISPLAYMODE.list){
				checkBox.setVisibility(View.GONE);
			}else{
				checkBox.setVisibility(View.VISIBLE);
				if(mMultSelected.contains(idStr)){
					checkBox.setChecked(true);
				}else{
					checkBox.setChecked(false);
				}
			}
			
			
			
			//�����ݰ󶨸��ؼ�
			if(name != null){
				header.setImageResource(R.drawable.ic_contact_picture);
				if(msg_count > 1){
					tv_name.setText(name + "(" + msg_count + ")");
				}else{
					tv_name.setText(name);
				}
			}else{
				header.setImageResource(R.drawable.ic_unknown_picture_normal);
				if(msg_count > 1){
					tv_name.setText(address + "(" + msg_count + ")");
				}else{
					tv_name.setText(address);
				}
			}
						
			


			//�����ڣ�1 ����ǽ�������ڣ���ô���Ǿ���ʾʱ�䣬������Ǿ���ʾ���ڡ�2 ��ʾ�ķ��Ӧ�ú�ϵͳ����һ��
			String dateStr = null;
			if((date - firstSecondOfToday > 0) && (date - firstSecondOfToday < DateUtils.DAY_IN_MILLIS)){
				//��ʾʱ��
				dateStr = DateFormat.getTimeFormat(context).format(date);
			}else{
				//��ʾ����
				dateStr = DateFormat.getDateFormat(context).format(date);
			}
			tv_date.setText(dateStr);
			
			
			tv_body.setText(body);
		}
		
	}
	
	/**
	 * ����һ�ε��menu��ʱ�����
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu_item_search = menu.add(0, MENU_SEARCH_ID, 0, R.string.search);
		menu_item_delete = menu.add(0, MENU_DELETE_ID, 0, R.string.delete);
		menu_item_back = menu.add(0, MENU_BACK_ID, 0, R.string.back);
		return super.onCreateOptionsMenu(menu);
	}
	
	
	/**
	 * �÷�����ÿ��menu�������¶��ᱻ����
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		Log.i("i", " onPrepareOptionsMenu ");
		//һ����ͨ�������ļ� MenuInflater
		//ͨ������ֱ����ʾ 
		if(mode == DISPLAYMODE.list){
			menu_item_search.setVisible(true);
			menu_item_delete.setVisible(true);
			menu_item_back.setVisible(false);
			
			if(mAdapter.getCount() > 0){
				menu_item_delete.setEnabled(true);
			}else{
				menu_item_delete.setEnabled(false);
			}
		}else{
			menu_item_search.setVisible(false);
			menu_item_delete.setVisible(false);
			menu_item_back.setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	/**
	 * menuItem�������ʱ�����
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		switch (id) {
			case MENU_SEARCH_ID:
				//����ϵͳ����������
				onSearchRequested();
				break;
			case MENU_DELETE_ID:
				changeMode(DISPLAYMODE.edit);
				break;
			case MENU_BACK_ID:
				changeMode(DISPLAYMODE.list);
				break;
	
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * �ı�activity����ʾģʽ
	 * @param mode 
	 * ��listViewλ�÷����ı䣬����listview�ػ棬���оͳ�����checkbox
	 */
	private void changeMode(DISPLAYMODE mode) {
		// TODO Auto-generated method stub
		this.mode = mode;
		if(mode == DISPLAYMODE.edit){
			bt_new_msg.setVisibility(View.GONE);
			mEdit.setVisibility(View.VISIBLE);
			bt_delete.setVisibility(View.VISIBLE);
			
			bt_all_select.setEnabled(true);
			bt_cancel_selected.setEnabled(false);
			bt_delete.setEnabled(false);
		}else{
			bt_new_msg.setVisibility(View.VISIBLE);
			mEdit.setVisibility(View.GONE);
			bt_delete.setVisibility(View.GONE);
			mMultSelected.clear();
		}

	}

	/**
	 * �����첽��ѯ��
	 *
	 */
	private final class QueryHandler extends AsyncQueryHandler{

		public QueryHandler(ContentResolver cr) {
			super(cr);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
//			String[] names = cursor.getColumnNames();
//			while(cursor.moveToNext()){
//				for(String name:names){
//					Log.i("i", name + ":"  + cursor.getString(cursor.getColumnIndex(name)));
//				}
//			}
			mAdapter.changeCursor(cursor);//ͨ��adapter���ݷ����ı�
		}
		
		
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
			case R.id.bt_new_msg:
				Intent intent = new Intent(this,NewMessageActivity.class);
				startActivity(intent);
				//mListView.setSelectionFromTop(2, 1);
				break;
			case R.id.bt_all_select:
				for(int i = 0;i< mAdapter.getCount();i++){
					Cursor cursor = (Cursor) mAdapter.getItem(i);
					String idStr = cursor.getString(ID_COLUMN_INDEX);
					mMultSelected.add(idStr);
				}
				mAdapter.notifyDataSetChanged();//��listView�Զ�ˢ��
				
				bt_all_select.setEnabled(false);
				bt_cancel_selected.setEnabled(true);
				bt_delete.setEnabled(true);
				break;
			case R.id.bt_cancel_selected:
				mMultSelected.clear();
				mAdapter.notifyDataSetChanged();
				bt_all_select.setEnabled(true);
				bt_cancel_selected.setEnabled(false);
				bt_delete.setEnabled(false);
				break;
			case R.id.bt_delete:
				/**
				 * 1 ����builder
				 * 2 ��builder�������� ���⡢��ʾ��Ϣ����ť
				 * 3����dialog
				 * 4��ʾdialog
				 */
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setCancelable(false);//���λ��˼�
				builder.setTitle(R.string.delete);
				builder.setMessage(R.string.delete_alert);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mProgressDialog = new ProgressDialog(ConversationActivity.this);
						mProgressDialog.setIcon(android.R.drawable.ic_dialog_alert);
						mProgressDialog.setTitle(R.string.delete);
						//���ý������Ի���ķ��
						mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						//���ý������Ĵ�С
						mProgressDialog.setMax(mMultSelected.size());
						mProgressDialog.setCancelable(false);
						mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							
							public void onDismiss(DialogInterface dialog) {
								// TODO Auto-generated method stub
								changeMode(DISPLAYMODE.list);
								//delete = true;
							}
						});
						mProgressDialog.setButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								delete = false;
								mProgressDialog.dismiss();
							}
						});
						mProgressDialog.show();
						
						delete = true;
						
						//ɾ���Ự
						new Thread(new DeleteTask()).start();
					}
				});
				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
				break;
	
			default:
				break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mode == DISPLAYMODE.edit){
				changeMode(DISPLAYMODE.list);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	//ɾ���Ự������
	private final class DeleteTask implements Runnable{

		public void run() {
			// TODO Auto-generated method stub
			
			ArrayList<String> list = new ArrayList<String>(mMultSelected);
			
			for(int i = 0;i< list.size();i++){
				
				if(!delete){
					return;
				}
				Uri uri = Uri.withAppendedPath(Sms.CONVERSATION_URI, list.get(i));
				getContentResolver().delete(uri, null, null);
				
				//���½���������ʾ
				mProgressDialog.incrementProgressBy(1);
				
				SystemClock.sleep(2000);
			}
			
			mProgressDialog.dismiss();
			//���̲߳��ܶ���ʾ���в���
			//changeMode(DISPLAYMODE.list);
		}
		
	}
	
	/**
	 * Ӧ�ð�cursor�ر�
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Cursor cursor = mAdapter.getCursor();
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
	}
}
