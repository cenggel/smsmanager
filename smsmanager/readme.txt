public class AutoCompleteTextView extends EditText implements Filter.FilterListener
��������˭����ɣ�Filter.FilterListener

addTextChangedListener(new MyWatcher()); ����ı��¼�����Ӧ

doAfterTextChanged();

            if (mFilter != null) {
                mPopupCanBeUpdated = true;
                performFiltering(getText(), mLastKeyCode); ִ�й���
            }
            
    protected void performFiltering(CharSequence text, int keyCode) {
        mFilter.filter(text, this);
    }   
    
�о� mFilter��ʲô��
    mAdapter == ContactAdapter == CursorAdapter
	Filter mFilter;Filter��һ��������   mFilter == new CursorFilter(this);
	mFilter�ĳ�ʼ����
	void setAdapter(T adapter) {
	mFilter = ((Filterable) mAdapter).getFilter();
	}
	
�о�CursorAdapter?
getFilter(){
        if (mCursorFilter == null) {
            mCursorFilter = new CursorFilter(this);
        }
        return mCursorFilter;
}


�о�CursorFilter?
class CursorFilter extends Filter 

�о�Filter?
��filter(text,FilterListener){
 ������һ�����̣߳��������߳����洴����һ��RequestHandler,���ҷ�����Ϣ
}

����Ȩ�͵���RequestHandler��
args.results = performFiltering(args.constraint);ִ�й��ˣ��÷�����һ�����󷽷�������ʵ����CursorFilter


�о�CursorFilter?
	Cursor cursor = mClient.runQueryOnBackgroundThread(constraint);

mClient��ʲô��
    CursorFilter(CursorFilterClient client) {
        mClient = client;
    }
    
   CursorFilterClient =  mClient = CursorAdapter == ContactAdapter
   
   
��ѯ��������������ν���AutoCompleteTextView�ģ�
                        message = mResultHandler.obtainMessage(what);
                        message.obj = args;
                        message.sendToTarget();
                        
                        ����Ȩ�ͽ�����ResultHandler��
                        publishResults(args.constraint, args.results);���������÷�����һ�����󷽷�������ʵ����CursorFilter
   


















