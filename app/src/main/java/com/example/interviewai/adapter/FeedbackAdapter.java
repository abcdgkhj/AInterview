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
        private final TextView txtItemScore;
        private final TextView txtItemStatusBadge;
        private final TextView txtItemQuestion;
        private final TextView txtItemAnswer;
        private final View layoutItemStrength;
        private final TextView txtItemStrength;
        private final View layoutItemImprovement;
        private final TextView txtItemGuideline;
        private final View layoutItemAiNotice;
        private final TextView txtItemAiNotice;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            txtItemNumber = itemView.findViewById(R.id.txtItemNumber);
            txtItemScore = itemView.findViewById(R.id.txtItemScore);
            txtItemStatusBadge = itemView.findViewById(R.id.txtItemStatusBadge);
            txtItemQuestion = itemView.findViewById(R.id.txtItemQuestion);
            txtItemAnswer = itemView.findViewById(R.id.txtItemAnswer);
            layoutItemStrength = itemView.findViewById(R.id.layoutItemStrength);
            txtItemStrength = itemView.findViewById(R.id.txtItemStrength);
            layoutItemImprovement = itemView.findViewById(R.id.layoutItemImprovement);
            txtItemGuideline = itemView.findViewById(R.id.txtItemGuideline);
            layoutItemAiNotice = itemView.findViewById(R.id.layoutItemAiNotice);
            txtItemAiNotice = itemView.findViewById(R.id.txtItemAiNotice);
        }

        public void bind(InterviewItem item, int index) {
            txtItemNumber.setText("Pertanyaan " + index);
            txtItemQuestion.setText(item.getQuestion());

            // Score badge for this specific question
            if (txtItemScore != null) {
                txtItemScore.setText("Skor: " + item.getScore());
            }

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

            // Strengths / Analysis feedback
            if (txtItemStrength != null) {
                String strength = item.getStrength();
                if (strength != null && !strength.trim().isEmpty()) {
                    txtItemStrength.setText(strength);
                    if (layoutItemStrength != null) layoutItemStrength.setVisibility(View.VISIBLE);
                } else {
                    if (layoutItemStrength != null) layoutItemStrength.setVisibility(View.GONE);
                }
            }

            // Improvement / Guideline feedback
            if (txtItemGuideline != null) {
                String improvement = item.getImprovement();
                if (improvement != null && !improvement.trim().isEmpty()) {
                    txtItemGuideline.setText(improvement);
                    if (layoutItemImprovement != null) layoutItemImprovement.setVisibility(View.VISIBLE);
                } else {
                    if (layoutItemImprovement != null) layoutItemImprovement.setVisibility(View.GONE);
                }
            }

            // Ideal sample answer / recommendation
            if (txtItemAiNotice != null) {
                String sampleAnswer = item.getSampleAnswer();
                if (sampleAnswer != null && !sampleAnswer.trim().isEmpty()) {
                    txtItemAiNotice.setText(sampleAnswer);
                    if (layoutItemAiNotice != null) layoutItemAiNotice.setVisibility(View.VISIBLE);
                } else {
                    if (layoutItemAiNotice != null) layoutItemAiNotice.setVisibility(View.GONE);
                }
            }
        }
    }
}
