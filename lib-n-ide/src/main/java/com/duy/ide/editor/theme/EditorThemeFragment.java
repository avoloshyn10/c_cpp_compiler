package com.duy.ide.editor.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.CharacterStyle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duy.ide.editor.Highlighter;
import com.duy.ide.editor.editor.R;
import com.duy.ide.editor.theme.model.EditorTheme;
import com.duy.ide.editor.view.IEditAreaView;
import com.jecelyin.editor.v2.Preferences;
import com.jecelyin.editor.v2.highlight.Buffer;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.gjt.sp.jedit.Catalog;
import org.gjt.sp.jedit.Mode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class EditorThemeFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private EditorThemeAdapter mEditorThemeAdapter;
    private Preferences mPreferences;
    private ProgressBar mProgressBar;
    private LoadThemeTask mLoadThemeTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editor_theme, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreferences = Preferences.getInstance(getContext());
        mProgressBar = view.findViewById(R.id.progress_bar);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        loadData();

    }

    private void loadData() {
        mLoadThemeTask = new LoadThemeTask(getContext());
        mLoadThemeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroyView() {
        if (mLoadThemeTask != null) {
            mLoadThemeTask.cancel(true);
        }
        super.onDestroyView();
    }

    private int findThemeIndex(EditorTheme editorTheme) {
        int position = mEditorThemeAdapter.getPosition(editorTheme);
        if (position < 0) {
            return 0;
        }
        return position;
    }

    public static class EditorThemeAdapter extends RecyclerView.Adapter<EditorThemeAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
        private final ArrayList<EditorTheme> mEditorThemes;
        private Context mContext;
        private OnThemeSelectListener onThemeSelectListener;
        private Mode mLanguage = Catalog.getModeByName("C++");

        EditorThemeAdapter(Context context) {
            mContext = context;
            mEditorThemes = ThemeLoader.getAll(context);
            Collections.sort(mEditorThemes, new Comparator<EditorTheme>() {
                @Override
                public int compare(EditorTheme o1, EditorTheme o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }

        int getPosition(EditorTheme editorTheme) {
            return mEditorThemes.indexOf(editorTheme);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_theme, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final EditorTheme editorTheme = mEditorThemes.get(position);

            final String title = makeTitle(position, editorTheme);
            holder.mTxtName.setText(title);
            IEditAreaView editorView = holder.mEditorView;

            Buffer buffer = new Buffer();
            Highlighter highlighter = new Highlighter();
            editorView.setTheme(editorTheme);

            buffer.setMode(mLanguage, mContext);
            editorView.setText("");
            editorView.getText().insert(0, getSampleData());

            buffer.setEditable(editorView.getText());
            buffer.insert(0, getSampleData());

            @SuppressLint("UseSparseArrays")
            HashMap<Integer, ArrayList<? extends CharacterStyle>> colorsMap = new HashMap<>();
            int lineCount = buffer.getLineManager().getLineCount();
            highlighter.highlight(buffer, editorTheme, colorsMap, editorView.getText(), 0, lineCount - 1);

            holder.mBtnSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onThemeSelectListener != null) {
                        onThemeSelectListener.onEditorThemeSelected(editorTheme);
                    }
                }
            });
        }

        private String makeTitle(int position, EditorTheme editorTheme) {
            return (position + 1) + ". " + editorTheme.getName();
        }

        @Override
        public int getItemCount() {
            return mEditorThemes.size();
        }

        private String getSampleData() {
            return "// C++ Program to Find Largest Element of an Array\n" +
                    "\n" +
                    "// This program takes n number of element from user (where, n is specified by user) and stores data in an array. Then, this program displays the largest element of that array using loops.\n" +
                    "\n" +
                    "#include <iostream>\n" +
                    "\n" +
                    "using namespace std;\n" +
                    "\n" +
                    "int main() {\n" +
                    "    int i, n;\n" +
                    "    float arr[100];\n" +
                    "\n" +
                    "    cout << \"Enter total number of elements(1 to 100): \";\n" +
                    "    cin >> n;\n" +
                    "    cout << endl;\n" +
                    "\n" +
                    "    // Store number entered by the user\n" +
                    "    for (i = 0; i < n; ++i) {\n" +
                    "        cout << \"Enter Number \" << i + 1 << \" : \";\n" +
                    "        cin >> arr[i];\n" +
                    "    }\n" +
                    "\n" +
                    "    // Loop to store largest number to arr[0]\n" +
                    "    for (i = 1; i < n; ++i) {\n" +
                    "        // Change < to > if you want to find the smallest element\n" +
                    "        if (arr[0] < arr[i])\n" +
                    "            arr[0] = arr[i];\n" +
                    "    }\n" +
                    "    cout << \"Largest element = \" << arr[0];\n" +
                    "\n" +
                    "    return 0;\n" +
                    "}";
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            return mEditorThemes.get(position).getName();
        }

        public void setOnThemeSelectListener(OnThemeSelectListener onThemeSelectListener) {
            this.onThemeSelectListener = onThemeSelectListener;
        }

        public interface OnThemeSelectListener {
            void onEditorThemeSelected(EditorTheme theme);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View mBtnSelect;
            IEditAreaView mEditorView;
            TextView mTxtName;

            public ViewHolder(View itemView) {
                super(itemView);
                mEditorView = itemView.findViewById(R.id.editor_view);
                mTxtName = itemView.findViewById(R.id.txt_name);
                mBtnSelect = itemView.findViewById(R.id.btn_select);
            }
        }
    }

    private class LoadThemeTask extends AsyncTask<Void, Void, Void> {
        private Context context;

        LoadThemeTask(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ThemeLoader.init(getContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isCancelled()) {
                return;
            }
            mEditorThemeAdapter = new EditorThemeAdapter(getContext());
            mEditorThemeAdapter.setOnThemeSelectListener((EditorThemeAdapter.OnThemeSelectListener) getActivity());
            mRecyclerView.setAdapter(mEditorThemeAdapter);
            mRecyclerView.scrollToPosition(findThemeIndex(mPreferences.getEditorTheme()));
            mProgressBar.setVisibility(View.GONE);
        }
    }
}