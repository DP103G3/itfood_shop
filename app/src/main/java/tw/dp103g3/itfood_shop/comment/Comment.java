package tw.dp103g3.itfood_shop.comment;

import java.util.Date;

public class Comment {
    private int cmt_id;
    private int cmt_score;
    private String cmt_detail;
    private int shop_id;
    private int mem_id;
    private int cmt_state;
    private String cmt_feedback;
    private Date cmt_time;
    private Date cmt_feedback_time;
    private int cmt_feedback_state;
    private boolean isExpanded;

    // full constructor
    public Comment(int cmt_id, int cmt_score, String cmt_detail, int shop_id, int mem_id, int cmt_state,
                   String cmt_feedback, Date cmt_time, Date cmt_feedback_time, int cmt_feedback_state) {
        super();
        this.cmt_id = cmt_id;
        this.cmt_score = cmt_score;
        this.cmt_detail = cmt_detail;
        this.shop_id = shop_id;
        this.mem_id = mem_id;
        this.cmt_state = cmt_state;
        this.cmt_feedback = cmt_feedback;
        this.cmt_time = cmt_time;
        this.cmt_feedback_time = cmt_feedback_time;
        this.cmt_feedback_state = cmt_feedback_state;
    }

    //member updating comment constructor
    public Comment(int cmt_score, String cmt_detail, int cmt_state) {
        super();
        this.cmt_score = cmt_score;
        this.cmt_detail = cmt_detail;
        this.cmt_state = cmt_state;
    }

    //shop updating comment constructor
    public Comment(String cmt_feedback, Date cmt_feedback_time, int cmt_feedback_state) {
        super();
        this.cmt_feedback = cmt_feedback;
        this.cmt_feedback_time = cmt_feedback_time;
        this.cmt_feedback_state = cmt_feedback_state;
    }

    //Constructor while member inserting.
    public Comment(int cmt_score, String cmt_detail, int shop_id, int mem_id, int cmt_state) {
        super();
        this.cmt_score = cmt_score;
        this.cmt_detail = cmt_detail;
        this.shop_id = shop_id;
        this.mem_id = mem_id;
        this.cmt_state = cmt_state;
    }


    public Date getCmt_time() {
        return cmt_time;
    }

    public void setCmt_time(Date cmt_time) {
        this.cmt_time = cmt_time;
    }

    public int getCmt_id() {
        return cmt_id;
    }

    public void setCmt_id(int cmt_id) {
        this.cmt_id = cmt_id;
    }

    public int getCmt_score() {
        return cmt_score;
    }

    public void setCmt_score(int cmt_score) {
        this.cmt_score = cmt_score;
    }

    public String getCmt_detail() {
        return cmt_detail;
    }

    public void setCmt_detail(String cmt_detail) {
        this.cmt_detail = cmt_detail;
    }

    public int getShop_id() {
        return shop_id;
    }

    public void setShop_id(int shop_id) {
        this.shop_id = shop_id;
    }

    public int getMem_id() {
        return mem_id;
    }

    public void setMem_id(int mem_id) {
        this.mem_id = mem_id;
    }

    public int getCmt_state() {
        return cmt_state;
    }

    public void setCmt_state(int cmt_state) {
        this.cmt_state = cmt_state;
    }

    public String getCmt_feedback() {
        return cmt_feedback;
    }

    public void setCmt_feedback(String cmt_feedback) {
        this.cmt_feedback = cmt_feedback;
    }

    public Date getCmt_feedback_time() {
        return cmt_feedback_time;
    }

    public void setCmt_feedback_time(Date cmt_feedback_time) {
        this.cmt_feedback_time = cmt_feedback_time;
    }

    public int getCmt_feedback_state() {
        return cmt_feedback_state;
    }

    public void setCmt_feedback_state(int cmt_feedback_state) {
        this.cmt_feedback_state = cmt_feedback_state;
    }


    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}

