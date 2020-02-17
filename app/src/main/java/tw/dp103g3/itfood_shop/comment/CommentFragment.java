package tw.dp103g3.itfood_shop.comment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import tw.dp103g3.itfood_shop.R;
import tw.dp103g3.itfood_shop.main.Common;
import tw.dp103g3.itfood_shop.main.Url;
import tw.dp103g3.itfood_shop.member.Member;
import tw.dp103g3.itfood_shop.task.CommonTask;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static tw.dp103g3.itfood_shop.main.Common.PREFERENCES_SHOP;
import static tw.dp103g3.itfood_shop.main.Common.networkConnected;
import static tw.dp103g3.itfood_shop.main.Common.setDialogUi;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentFragment extends Fragment {
    private final int WEEK = 1;
    private final int MONTH = 2;
    private final int YEAR = 3;
    private final int ALL = 4;
    private Activity activity;
    private RecyclerView rvComments;
    private int shop_id;
    private SharedPreferences pref;
    private MaterialButton btTime;
    private ConstraintLayout layoutComments, layoutNoComments;
    private List<Comment> comments;
    private String TAG = "TAG_CommentFragment";
    private int selectedTime = 0;


    public CommentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

        pref = activity.getSharedPreferences(PREFERENCES_SHOP, MODE_PRIVATE);
        shop_id = pref.getInt("shopId", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Common.disconnectServer();
        int checkedTime = R.id.btWeek;
        rvComments = view.findViewById(R.id.rvComments);
        layoutComments = view.findViewById(R.id.layoutComments);
        layoutNoComments = view.findViewById(R.id.layoutNoComments);
        layoutComments.setVisibility(GONE);
        layoutNoComments.setVisibility(GONE);
        btTime = view.findViewById(R.id.btTime);
        rvComments.setLayoutManager(new LinearLayoutManager(activity));
        rvComments.setPadding(0, 0, 0, activity.findViewById(R.id.bottomNavigation).getHeight() + 24);
        comments = getComments();
        selectedTime = WEEK;
        if (comments.isEmpty()) {
            layoutNoComments.setVisibility(View.VISIBLE);
            layoutComments.setVisibility(View.VISIBLE);
        } else {
            layoutComments.setVisibility(View.VISIBLE);
        }
        ShowComments(comments, selectedTime);
        setBtTime();
    }

    private void swapView(ConstraintLayout v, boolean show) {
        ConstraintSet csOld = new ConstraintSet();
        ConstraintSet csNew = new ConstraintSet();
        csOld.clone(activity, R.layout.comment_reply_not_expanded_view);
        csNew.clone(activity, R.layout.comment_reply_expanded_view);
        if (show) {
            csNew.applyTo(v);
        } else {
            csOld.applyTo(v);
        }
    }

    private boolean toggleLayout(boolean isExpanded, View v, ConstraintLayout cl) {
        CommentAnimations.toggleArrow(v, isExpanded);
        if (isExpanded) {
            CommentAnimations.expand(cl);
        } else {
            CommentAnimations.collapse(cl);
        }
        return isExpanded;
    }

    private Member getMember(int mem_id) {
        Member member = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/MemberServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat(Common.DATE_FORMAT).create();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getMemberTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMemberTask.execute().get();
                member = gson.fromJson(jsonIn, Member.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return member;
    }

    private List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/CommentServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "findByCaseWithState");
            jsonObject.addProperty("id", shop_id);
            jsonObject.addProperty("type", "shop");
            jsonObject.addProperty("state", 1);
            String jsonOut = jsonObject.toString();
            try {
                String jsonIn = new CommonTask(url, jsonOut).execute().get();
                Type listType = new TypeToken<List<Comment>>() {
                }.getType();
                comments = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return comments;
    }

    private void ShowComments(List<Comment> comments, int selectedTime) {
        if (comments == null || comments.isEmpty()) {
            if (Common.networkConnected(activity)) {
                layoutNoComments.setVisibility(View.VISIBLE);
                Common.showToast(activity, R.string.textNoComments);
            } else {
                layoutNoComments.setVisibility(GONE);
                Common.showToast(activity, R.string.textNoNetwork);
            }
        }
        Calendar calendar = Calendar.getInstance();
        if (selectedTime == WEEK) {
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            comments = comments.stream().sorted(Comparator.comparing(Comment::getCmt_feedback_state).reversed()).filter(comment -> comment.getCmt_time().getTime() > calendar.getTime().getTime()).collect(Collectors.toList());
        } else if (selectedTime == MONTH) {
            calendar.add(Calendar.MONTH, -1);
            comments = comments.stream().sorted(Comparator.comparing(Comment::getCmt_feedback_state).reversed()).filter(comment -> comment.getCmt_time().getTime() > calendar.getTime().getTime()).collect(Collectors.toList());
        } else if (selectedTime == YEAR) {
            calendar.add(Calendar.YEAR, -1);
            comments = comments.stream().sorted(Comparator.comparing(Comment::getCmt_feedback_state).reversed()).filter(comment -> comment.getCmt_time().getTime() > calendar.getTime().getTime()).collect(Collectors.toList());
        } else if (selectedTime == ALL) {
            comments = comments.stream().sorted(Comparator.comparing(Comment::getCmt_feedback_state).reversed()).collect(Collectors.toList());
        }

        CommentAdapter commentAdapter = (CommentAdapter) rvComments.getAdapter();
        if (commentAdapter == null) {
            rvComments.setAdapter(new CommentAdapter(activity, comments));
        } else {
            commentAdapter.setComments(comments);
            commentAdapter.notifyDataSetChanged();
        }
    }

    private void setBtTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = View.inflate(activity, R.layout.comment_select_time_dialog, null);
        builder.setView(dialogView);
        Dialog dialog = builder.create();
        MaterialButton btWeek, btMonth, btYear, btAll;
        btWeek = dialogView.findViewById(R.id.btWeek);
        btMonth = dialogView.findViewById(R.id.btMonth);
        btYear = dialogView.findViewById(R.id.btYear);
        btAll = dialogView.findViewById(R.id.btAll);
        btWeek.setOnClickListener(v -> {
            btTime.setText("最近一週");
            selectedTime = WEEK;
            ShowComments(getComments(), selectedTime);
            dialog.dismiss();
        });
        btAll.setOnClickListener(v -> {
            btTime.setText("過去所有");
            selectedTime = ALL;
            ShowComments(getComments(), selectedTime);
            dialog.dismiss();
        });
        btMonth.setOnClickListener(v -> {
            btTime.setText("最近一個月");
            selectedTime = MONTH;
            ShowComments(getComments(), selectedTime);
            dialog.dismiss();
        });
        btYear.setOnClickListener(v -> {
            btTime.setText("最近一年");
            selectedTime = YEAR;
            ShowComments(getComments(), selectedTime);
            dialog.dismiss();
        });

        btTime.setOnClickListener(v -> {
            setDialogUi(dialog, activity);
            dialog.show();
        });
    }

    private void deleteComment(Comment comment, DialogInterface dialog) {
        dialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("刪除回覆");
        builder.setMessage("你確定要刪除此筆回覆？");
        builder.setNegativeButton("取消", (dialog1, which) -> dialog1.cancel());
        builder.setPositiveButton("確定", (dialog12, which) -> {
            Gson gson = Common.gson;
            String url = Url.URL + "/CommentServlet";
            int count = 0;
            comment.setCmt_feedback_state(0);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "reply");
            jsonObject.addProperty("comment", gson.toJson(comment, Comment.class));
            String jsonOut = jsonObject.toString();
            if (networkConnected(activity)) {
                try {
                    count = Integer.valueOf(new CommonTask(url, jsonOut).execute().get());
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            } else {
                Common.showToast(activity, R.string.textNoNetWork);
            }
            if (count != 0) {
                Common.showToast(activity, "刪除成功");
                comments = getComments();
                ShowComments(comments, selectedTime);
                dialog12.dismiss();
            } else {
                Common.showToast(activity, "刪除失敗，請稍後再試");
            }
        });
        Dialog dialog3 = builder.create();
        setDialogUi(dialog3, activity);
        dialog3.show();
    }

    private void editComment(Comment comment, DialogInterface dialog) {
        dialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View viewEdit = View.inflate(activity, R.layout.comment_edit_dialog, null);
        TextInputLayout textInputLayoutReply = viewEdit.findViewById(R.id.textInputLayoutReply);
        textInputLayoutReply.getEditText().setHint("評論回覆");
        if (comment.getCmt_feedback_state() != 0) {
            textInputLayoutReply.getEditText().setText(comment.getCmt_feedback() == null ? "" : comment.getCmt_feedback());
        }
        builder.setView(viewEdit);
        builder.setNegativeButton(R.string.textCancel, (dialog1, which) -> dialog1.cancel());
        builder.setPositiveButton(R.string.textConfirm, (dialog12, which) -> {
            String commentText = textInputLayoutReply.getEditText().getText().toString().trim();
            if (commentText.isEmpty()) {
                textInputLayoutReply.setError("請輸入回覆內容");
                return;
            } else {
                textInputLayoutReply.setError(null);
                Gson gson = Common.gson;
                String url = Url.URL + "/CommentServlet";
                Calendar calendar = Calendar.getInstance();
                comment.setCmt_feedback_time(calendar.getTime());
                comment.setCmt_feedback(commentText);
                comment.setCmt_feedback_state(1);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "reply");
                jsonObject.addProperty("comment", gson.toJson(comment, Comment.class));
                String jsonOut = jsonObject.toString();
                int count = 0;
                if (networkConnected(activity)) {
                    try {
                        count = Integer.valueOf(new CommonTask(url, jsonOut).execute().get());
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                } else {
                    Common.showToast(activity, R.string.textNoNetWork);
                }
                if (count != 0) {
                    Common.showToast(activity, "編輯成功");
                    comments = getComments();
                    ShowComments(comments, selectedTime);
                    dialog12.dismiss();
                } else {
                    Common.showToast(activity, "編輯失敗，請稍後再試");
                }
            }
        });
        Dialog comment_edit_dialog = builder.create();
        setDialogUi(comment_edit_dialog, activity);
        comment_edit_dialog.show();
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
        private Context context;
        private List<Comment> comments;

        CommentAdapter(Context context, List<Comment> comments) {
            this.context = context;
            this.comments = comments;
        }

        void setComments(List<Comment> comments) {
            this.comments = comments;
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Comment comment = comments.get(position);
            holder.layoutReply.setVisibility(GONE);
            Member member = getMember(comment.getMem_id());
            String rawMemberEmail = member.getMemEmail();
            String[] splitEmail = rawMemberEmail.split("@");
            //將會員的電子郵件由“＠”分開為兩部分，取前面顯示
            String memberEmail = splitEmail[0];
            holder.tvUsername.setText(memberEmail);
            holder.ratingBar.setRating(comment.getCmt_score());
            holder.tvCommentDetail.setText(comment.getCmt_detail());
            holder.tvCommentTime.setText(new SimpleDateFormat("MM月 dd, yyyy", Locale.getDefault()).format(comment.getCmt_time()));

            if (comment.getCmt_feedback() == null || comment.getCmt_feedback().isEmpty() || comment.getCmt_feedback_state() == 0) {
                holder.layoutExpandable.setVisibility(GONE);
            } else {
                holder.layoutExpandable.setVisibility(View.VISIBLE);
                holder.tvShopName.setText("你");
                holder.tvReplayContent.setText(comment.getCmt_feedback());
                holder.tvReplyDate.setText(new SimpleDateFormat("MM月 dd, yyyy", Locale.getDefault()).format(comment.getCmt_feedback_time()));
                View.OnClickListener onClickListener = v -> {
                    boolean show = toggleLayout(!comment.isExpanded(), holder.ibExpandable, holder.layoutReply);
                    swapView(holder.layoutExpandable, show);
                    comment.setExpanded(show);
                };
                holder.layoutExpandable.setOnClickListener(onClickListener);
                holder.ibExpandable.setOnClickListener(onClickListener);
                holder.tvExpandReply.setOnClickListener(onClickListener);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("評論選項");
            if (comment.getCmt_feedback() == null || comment.getCmt_feedback().isEmpty() || comment.getCmt_feedback_state() == 0) {
                String[] options = new String[]{"回覆評論"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            editComment(comment, dialog);
                        }
                    }
                });
            } else {
                String[] options = new String[]{"刪除回覆", "回覆評論"};
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        deleteComment(comment, dialog);
                    } else {
                        editComment(comment, dialog);
                    }
                });
            }
            Dialog dialog = builder.create();
            holder.ibOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDialogUi(dialog, activity);
                    dialog.show();
                }
            });
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.comment_item_view, parent, false);
            return new MyViewHolder(itemView);
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvUsername, tvCommentTime, tvCommentDetail, tvShopName, tvReplyDate, tvReplayContent, tvExpandReply;
            ImageButton ibExpandable, ibOptions;
            RatingBar ratingBar;
            ConstraintLayout layoutReply, layoutExpandable;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ibOptions = itemView.findViewById(R.id.ibOptions);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
                tvCommentDetail = itemView.findViewById(R.id.tvCommentDetail);
                ratingBar = itemView.findViewById(R.id.ratingBar);
                layoutReply = itemView.findViewById(R.id.layoutReply);
                layoutExpandable = itemView.findViewById(R.id.layoutExpandable);
                tvReplyDate = itemView.findViewById(R.id.tvReplyDate);
                tvShopName = itemView.findViewById(R.id.tvShopName);
                tvReplayContent = itemView.findViewById(R.id.tvReplyContent);
                tvExpandReply = itemView.findViewById(R.id.tvExpandReply);
                ibExpandable = itemView.findViewById(R.id.ibExpandable);
            }
        }
    }
}
