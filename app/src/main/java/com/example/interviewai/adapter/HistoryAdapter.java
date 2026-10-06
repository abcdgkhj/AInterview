package com.example.interviewai.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewai.R;
import com.example.interviewai.ResultActivity;
import com.example.interviewai.data.InterviewStorage;
import com.example.interviewai.model.InterviewSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface OnHistoryChangedListener {
        void onHistoryChanged();
    }

    private final Context context;
    private final List<InterviewSession> sessionList;
    private OnHistoryChangedListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));

    public HistoryAdapter(Context context, List<InterviewSession> sessionList) {
        this.context = context;
        this.sessionList = sessionList;
    }

    public void setOnHistoryChangedListener(OnHistoryChangedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        InterviewSession session = sessionList.get(position);
        holder.bind(session);

        // Tap: open session result
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ResultActivity.class);
            intent.putExtra(ResultActivity.EXTRA_SESSION, session);
            intent.putExtra("extra_session_id", session.getSessionId());
            context.startActivity(intent);
        });

        // Long-press: confirm then delete this entry
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Entri Ini?")
                    .setMessage("Catatan sesi " + session.getRoleCategory() + " ini akan dihapus secara permanen.")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_ID && adapterPosition < sessionList.size()) {
                            InterviewStorage.deleteSession(context, session.getSessionId());
                            sessionList.remove(adapterPosition);
                            notifyItemRemoved(adapterPosition);
                            notifyItemRangeChanged(adapterPosition, sessionList.size());
                            if (listener != null) {
                                listener.onHistoryChanged();
                            }
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return sessionList != null ? sessionList.size() : 0;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtHistoryRole;
        private final TextView txtHistoryDate;
        private final TextView txtHistoryMeta;
        private final TextView txtHistoryCandidate;
        private final TextView txtHistoryScore;
        private final TextView txtHistoryStatus;
        private final TextView txtHistorySnippet;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtHistoryRole = itemView.findViewById(R.id.txtHistoryRole);
            txtHistoryDate = itemView.findViewById(R.id.txtHistoryDate);
            txtHistoryMeta = itemView.findViewById(R.id.txtHistoryMeta);
            txtHistoryCandidate = itemView.findViewById(R.id.txtHistoryCandidate);
            txtHistoryScore = itemView.findViewById(R.id.txtHistoryScore);
            txtHistoryStatus = itemView.findViewById(R.id.txtHistoryStatus);
            txtHistorySnippet = itemView.findViewById(R.id.txtHistorySnippet);
        }

        public void bind(InterviewSession session) {
            txtHistoryRole.setText(session.getRoleCategory());
            txtHistoryDate.setText(dateFormat.format(new Date(session.getTimestamp())));

            int count = session.getTotalQuestions();
            String diff = session.getDifficulty() != null ? session.getDifficulty() : "Intermediate";
            String lang = "English".equalsIgnoreCase(session.getLanguage()) ? "EN" : "ID";
            if (txtHistoryMeta != null) {
                txtHistoryMeta.setText(count + " Pertanyaan \u2022 " + diff + " \u2022 " + lang);
            }

            txtHistoryCandidate.setText("Kandidat: " + session.getCandidateName());
            txtHistoryScore.setText("Skor: " + session.getOverallScore());
            txtHistoryStatus.setText("Status: " + session.getSummaryStatus());
            if (txtHistorySnippet != null) {
                String feedback = session.getSummaryFeedback();
                if (feedback != null && feedback.length() > 100) {
                    feedback = feedback.substring(0, 97) + "...";
                }
                txtHistorySnippet.setText(feedback);
            }
        }
    }
}
