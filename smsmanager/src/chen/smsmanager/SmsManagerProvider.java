package chen.smsmanager;

import java.util.HashMap;
import java.util.Map;

import chen.smsother.Groups;
import chen.smsother.Thread_Groups;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class SmsManagerProvider extends ContentProvider {

	private final static String authority = "chen.smsmanager.SmsManagerProvider";
	//�ȶ����ܹ����ʵ�uri root : content://authority
	private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private final static int GROUPS = 11;
	private final static int THREAD_GROUPS = 21;
	static{
		matcher.addURI(authority, "groups", GROUPS);
		//	content://cn.itcast.smsmanager.SmsManagerProvider/groups
		matcher.addURI(authority, "thread_groups", THREAD_GROUPS);
		//	content://cn.itcast.smsmanager.SmsManagerProvider/thread_groups
	}
	
	
	private static Map<String,String> mGroupsProjectionMap;
	private static Map<String,String> mThreadGroupsProjectionMap;
	static{
		mGroupsProjectionMap = new HashMap<String, String>();
		mGroupsProjectionMap.put(Groups._ID, Groups._ID);
		mGroupsProjectionMap.put(Groups.GROUP_NAME, Groups.GROUP_NAME);
		
		mThreadGroupsProjectionMap = new HashMap<String, String>();
		mThreadGroupsProjectionMap.put(Thread_Groups._ID, Thread_Groups._ID);
		mThreadGroupsProjectionMap.put(Thread_Groups.THREAD_ID, Thread_Groups.THREAD_ID);
		mThreadGroupsProjectionMap.put(Thread_Groups.GROUP_ID, Thread_Groups.GROUP_ID);
	}
	
	private SQLiteOpenHelper mOpenHelper;
	
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mOpenHelper = SmsManagerDBHelper.getInstance(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projectionIn, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		int code = matcher.match(uri);
		switch (code) {
			case GROUPS:
				qb.setTables("groups");
				qb.setProjectionMap(mGroupsProjectionMap);
				break;
			case THREAD_GROUPS:
				qb.setTables("thread_groups");
				qb.setProjectionMap(mThreadGroupsProjectionMap);
				break;
	
			default:
				throw new IllegalArgumentException("û��ƥ���uri " + uri);
			}
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = qb.query(db, projectionIn, selection, selectionArgs, null, null, sortOrder);
		//��cursor����һ��uri�ĸı��֪ͨ
		ret.setNotificationUri(getContext().getContentResolver(), uri);
		return ret;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		Uri result_uri = null;
		long id;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int code = matcher.match(uri);
		switch (code) {
			case GROUPS:
				id = db.insert("groups", Groups._ID, values);
				result_uri = ContentUris.withAppendedId(uri, id);
				break;
			case THREAD_GROUPS:
				id = db.insert("thread_groups", Thread_Groups._ID, values);
				result_uri = ContentUris.withAppendedId(uri, id);
				break;
	
			default:
				throw new IllegalArgumentException("û��ƥ���uri " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return result_uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
