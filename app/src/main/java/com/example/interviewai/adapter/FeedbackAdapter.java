package com.example.interviewai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewai.R;
import com.example.interviewai.ai.EvaluationStatus;
import com.example.interviewai.model.InterviewItem;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private final List<InterviewItem> itemList;

    public FeedbackAdapter(List<InterviewItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        InterviewItem item = itemList.get(position);
        holder.bind(item, position + 1);
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtItemNumber;
        private final TextView txtItemStatusBadge;
        private final TextView txtItemQuestion;
        private final TextView txtItemAnswer;
        private final TextView txtItemGuideline;
        private final TextView txtItemAiNotice;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            txtItemNumber = itemView.findViewById(R.id.txtItemNumber);
            txtItemStatusBadge = itemView.findViewById(R.id.txtItemStatusBadge);
            txtItemQuestion = itemView.findViewById(R.id.txtItemQuestion);
            txtItemAnswer = itemView.findViewById(R.id.txtItemAnswer);
            txtItemGuideline = itemView.findViewById(R.id.txtItemGuideline);
            txtItemAiNotice = itemView.findViewById(R.id.txtItemAiNotice);
        }

        public void bind(InterviewItem item, int index) {
            txtItemNumber.setText("Pertanyaan " + index);
            txtItemQuestion.setText(item.getQuestion());

            // Answer status badge
            String answer = item.getUserAnswer();
            boolean hasAnswer = answer != null && !answer.trim().isEmpty();

            if (hasAnswer) {
                txtItemStatusBadge.setText("Terjawab");
                txtItemStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success));
                txtItemStatusBadge.setBackgroundResource(R.drawable.bg_badge_role);
                txtItemAnswer.setText(answer);
                txtItemAnswer.setAlpha(1.0f);
            } else {
                txtItemStatusBadge.setText("Tidak Dijawab");
                txtItemStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.warning));
                txtItemStatusBadge.setBackgroundResource(R.drawable.bg_badge_score);
                txtItemAnswer.setText("(Pertanyaan ini dilewati / tidak dijawab)");
                txtItemAnswer.setAlpha(0.6f);
            }

            // Reference guideline (from question tip stored in improvement field)
            txtItemGuideline.setText(item.getImprovement());

            // AI evaluation status notice
            // Reads the actual EvaluationStatus enum value from the item.
            // When a real AI service sets COMPLETED status in the future,
            // replace this switch to display actual AI feedback fields instead.
            EvaluationStatus evalStatus = item.getEvaluationStatus();
            String aiNoticeText;

            switch (evalStatus) {
                case COMPLETED:
                    // Future: display real AI feedback from EvaluationResponse here
                    aiNoticeText = item.getSampleAnswer();
                    break;
                case PENDING:
                    aiNoticeText = "Evaluasi AI: Sedang diproses...";
                    break;
                case FAILED:
                    aiNoticeText = "Evaluasi AI: Gagal. Coba lagi nanti.";
                    break;
                case NOT_AVAILABLE:
                case NOT_REQUESTED:
                default:
                    // Use the placeholder text stored by InterviewWorkflowManager
                    aiNoticeText = item.getSampleAnswer();
                    if (aiNoticeText == null || aiNoticeText.trim().isEmpty()) {
                        aiNoticeText = "Evaluasi AI: Belum tersedia (menunggu integrasi layanan AI).";
                    }
                    break;
            }
            txtItemAiNotice.setText(aiNoticeText);
        }
    }
}
