/**
 *
 */
package com.mingseal.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mingseal.application.UserApplication;
import com.mingseal.communicate.SocketInputThread;
import com.mingseal.communicate.SocketThreadManager;
import com.mingseal.data.dao.WeldLineMidDao;
import com.mingseal.data.param.PointConfigParam;
import com.mingseal.data.param.SettingParam;
import com.mingseal.data.point.Point;
import com.mingseal.data.point.weldparam.PointWeldLineMidParam;
import com.mingseal.dhp_500dh.R;
import com.mingseal.listener.MaxMinEditWatcher;
import com.mingseal.listener.MaxMinFocusChangeListener;
import com.mingseal.listener.MyPopWindowClickListener;
import com.mingseal.ui.PopupListView;
import com.mingseal.ui.PopupView;
import com.mingseal.utils.SharePreferenceUtils;
import com.mingseal.utils.ToastUtil;
import com.zhy.autolayout.AutoLayoutActivity;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author wangjian
 */
public class WeldLineMidActivity extends AutoLayoutActivity implements OnClickListener {

    private final static String TAG = "WeldLineMidActivity";
    /**
     * 标题栏的标题
     */
    private TextView tv_title;

    /**
     * 中间点的方案Spinner
     */
    private Spinner lineMidSpinner;

    /**
     * 返回上级菜单
     */
    private RelativeLayout rl_back;

    /**
     * 保存方案按钮
     */
    private RelativeLayout rl_save;
    /**
     * 完成按钮
     */
    private RelativeLayout rl_complete;
    private Intent intent;
    private Point point;// 从taskActivity中传值传过来的point
    private int mFlag;// 0代表增加数据，1代表更新数据
    private int mType;// 1表示要更新数据

    private WeldLineMidDao weldMidDao;
    private List<PointWeldLineMidParam> weldMidLists;
    private PointWeldLineMidParam weldMid;
    private int param_id = 1;// / 选取的是几号方案
    /**
     * @Fields moveSpeedInt: 轨迹速度的int值
     */
    private int moveSpeedInt = 0;
    /**
     * @Fields isNull: 判断编辑输入框是否为空,false表示为空,true表示不为空
     */
    private boolean isNull = false;
    private boolean flag = false;// 可以与用户交互，初始化完成标志

    /* =================== begin =================== */
    private HashMap<Integer, PointWeldLineMidParam> update_id;// 修改的方案号集合
    private int defaultNum = 1;// 默认号
    ArrayList<PopupView> popupViews;
    private TextView mMorenTextView;
    PopupListView popupListView;
    int p = 0;
    View extendView;
    private ToggleButton switch_isSn;
    private EditText et_linemid_moveSpeed;
    private EditText et_sendSnSpeed;



    private RelativeLayout rl_moren;
    private ImageView iv_add;
    private ImageView iv_moren;

    private boolean isOk;
    private boolean isExist = false;// 是否存在
    private boolean firstExist = false;// 是否存在
    /**
     * 当前任务号
     */
    private int currentTaskNum;
    private int currentClickNum;// 当前点击的序号
    private int mIndex;// 对应方案号

    private TextView tv_moveSpeed;
    private TextView extend_mms;
    private TextView extend_isSn;
    private TextView tv_radius;
    private TextView extend_mm;
    private TextView tv_stopSnTime;
    private TextView extend_mm2;
    private TextView tv_stopDisNext;
    private TextView extend_mm3;
    private TextView extend_save;
    private TextView extend_default;
    private TextView mFanganliebiao;
    private RevHandler handler;
    private UserApplication userApplication;
    /* =================== end =================== */
    private ViewStub stub_glue;
    private int Activity_Init_View = 3;
    private ImageView iv_loading;
    private TextView tv_sendSnSpeed;
    private EditText et_stopSnTime;
    private int sendSnSpeed;
    private int stopSnTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_glue_line_mid);
        userApplication = (UserApplication) getApplication();
        handler = new RevHandler();
        update_id = new HashMap<>();
        intent = getIntent();
        point = intent
                .getParcelableExtra(MyPopWindowClickListener.POPWINDOW_KEY);
        mFlag = intent.getIntExtra(MyPopWindowClickListener.FLAG_KEY, 0);
        mType = intent.getIntExtra(MyPopWindowClickListener.TYPE_KEY, 0);
        defaultNum = SharePreferenceUtils.getParamNumberFromPref(
                WeldLineMidActivity.this,
                SettingParam.DefaultNum.ParamGlueLineMidNumber);
        weldMidDao = new WeldLineMidDao(WeldLineMidActivity.this);
        weldMidLists = weldMidDao.findAllWeldLineMidParams();
        if (weldMidLists == null || weldMidLists.isEmpty()) {
            weldMid = new PointWeldLineMidParam();
            weldMid.set_id(param_id);
            weldMidDao.insertWeldLineMid(weldMid);
            // 插入主键id
        }
        weldMidLists = weldMidDao.findAllWeldLineMidParams();

        popupViews = new ArrayList<>();
        initPicker();
        Log.d(TAG, weldMidLists.toString());
    }

    /**
     * @param
     * @Title UpdateInfos
     * @Description 更新extendView数据（保存的数据）
     * @author wj
     */
    private void UpdateInfos(PointWeldLineMidParam weldLineMidParam) {
        if (weldLineMidParam == null) {
            et_linemid_moveSpeed.setText("");
            et_sendSnSpeed.setText("");
            et_stopSnTime.setText("");

        } else {
            et_linemid_moveSpeed.setText(weldLineMidParam.getMoveSpeed() + "");
            et_sendSnSpeed.setText(weldLineMidParam.getSendSnSpeed() + "");
            et_stopSnTime.setText(weldLineMidParam.getStopSnTime() + "");

            switch_isSn.setChecked(weldLineMidParam.isSn());
        }
    }

    /**
     * 加载自定义组件并设置NumberPicker的最大最小值
     */
    private void initPicker() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(getResources().getString(
                R.string.activity_glue_line_mid));
        mMorenTextView = (TextView) findViewById(R.id.morenfangan);
        mFanganliebiao = (TextView) findViewById(R.id.fanganliebiao);
        mFanganliebiao.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(50));
        mMorenTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(30));
        rl_back = (RelativeLayout) findViewById(R.id.rl_back);
        mMorenTextView.setText("当前默认方案号(" + defaultNum + ")");
        iv_loading = (ImageView) findViewById(R.id.iv_loading);
        iv_loading.setVisibility(View.VISIBLE);
        stub_glue = (ViewStub) findViewById(R.id.stub_glue_line_mid);
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                Message msg = new Message();
                msg.what = Activity_Init_View;
                handler.sendMessage(msg);
            }
        }, 50);// 延迟1毫秒,然后加载

    }

    protected void setTitleInfos(List<PointWeldLineMidParam> weldMidLists,
                                 View view, int p) {
//        title_moveSpeed = (TextView) view.findViewById(R.id.title_moveSpeed);
//        title_et_linemid_moveSpeed = (TextView) view.findViewById(R.id.title_et_linemid_moveSpeed);
//        activity_mm_s = (TextView) view.findViewById(R.id.activity_mm_s);
//        activity_fenghao = (TextView) view.findViewById(R.id.activity_fenghao);
//        title_activity_glue_radius = (TextView) view.findViewById(R.id.title_activity_glue_radius);
//        title_et_radius = (TextView) view.findViewById(R.id.title_et_radius);
//        activity_mm = (TextView) view.findViewById(R.id.activity_mm);
//        activity_second_fenghao = (TextView) view.findViewById(R.id.activity_second_fenghao);
//        title_stopDisPrev = (TextView) view.findViewById(R.id.title_stopDisPrev);
//        title_et_stopDisPrev = (TextView) view.findViewById(R.id.title_et_stopDisPrev);
//        activity_second_mm = (TextView) view.findViewById(R.id.activity_second_mm);
//        activity_third_fenghao = (TextView) view.findViewById(R.id.activity_third_fenghao);
//        title_stopDisNext = (TextView) view.findViewById(R.id.title_stopDisNext);
//        title_et_stopDisNext = (TextView) view.findViewById(R.id.title_et_stopDisNext);
//        activity_third_mm = (TextView) view.findViewById(R.id.activity_third_mm);
//        activity_four_fenghao = (TextView) view.findViewById(R.id.activity_four_fenghao);
//        title_activity_glue_port = (TextView) view.findViewById(R.id.title_activity_glue_port);
//        title_et_activity_glue_port = (TextView) view.findViewById(R.id.title_et_activity_glue_port);
//        for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
//            if (p == pointWeldLineMidParam.get_id()) {
//                    /*===================== begin =====================*/
//                title_moveSpeed.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                title_et_linemid_moveSpeed.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                activity_mm_s.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                activity_fenghao.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                title_activity_glue_radius.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                title_et_radius.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                activity_mm.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                activity_second_fenghao.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                title_stopDisPrev.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                title_et_stopDisPrev.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                activity_second_mm.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                activity_third_fenghao.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                title_stopDisNext.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                title_et_stopDisNext.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                activity_third_mm.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                activity_four_fenghao.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                title_activity_glue_port.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//                title_et_activity_glue_port.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(35));
//				/*=====================  end =====================*/
//                title_moveSpeed.setText(getResources().getString(
//                        R.string.activity_glue_moveSpeed)
//                        + " ");
//                activity_mm_s.setText(getResources().getString(
//                        R.string.activity_mm_s));
//                activity_fenghao.setText(getResources().getString(
//                        R.string.activity_fenghao)
//                        + " ");
//                title_activity_glue_radius.setText(getResources().getString(
//                        R.string.activity_glue_radius)
//                        + " ");
//                activity_mm.setText(getResources().getString(
//                        R.string.activity_mm));
//                activity_second_fenghao.setText(getResources().getString(
//                        R.string.activity_fenghao)
//                        + " ");
//                title_stopDisPrev.setText(getResources().getString(
//                        R.string.activity_glue_stopGlueDisPrev)
//                        + " ");
//                activity_second_mm.setText(getResources().getString(
//                        R.string.activity_mm));
//                activity_third_fenghao.setText(getResources().getString(
//                        R.string.activity_fenghao)
//                        + " ");
//                title_stopDisNext.setText(getResources()
//                        .getString(R.string.activity_glue_stopGlueDisNext)
//                        + " ");
//                activity_third_mm.setText(getResources().getString(R.string.activity_mm));
//                activity_four_fenghao.setText(getResources().getString(
//                        R.string.activity_fenghao)
//                        + " ");
//                title_activity_glue_port.setText(getResources().getString(
//                        R.string.activity_glue_port)
//                        + " ");
//                activity_four_fenghao.setText(getResources().getString(
//                        R.string.activity_fenghao)
//                        + " ");
//
//                for (int j = 0; j < 5; j++) {
//                    if (pointWeldLineMidParam.getGluePort()[j]) {
//                        GluePort[j] = "开";
//                    } else {
//                        GluePort[j] = "关";
//                    }
//                }
//
//                title_et_linemid_moveSpeed.getPaint().setFlags(
//                        Paint.UNDERLINE_TEXT_FLAG); // 下划线
//                title_et_linemid_moveSpeed.getPaint()
//                        .setAntiAlias(true); // 抗锯齿
//                title_et_radius.getPaint().setFlags(
//                        Paint.UNDERLINE_TEXT_FLAG); // 下划线
//                title_et_radius.getPaint().setAntiAlias(true); // 抗锯齿
//                title_et_stopDisPrev.getPaint().setFlags(
//                        Paint.UNDERLINE_TEXT_FLAG); // 下划线
//                title_et_stopDisPrev.getPaint().setAntiAlias(true); // 抗锯齿
//                title_et_stopDisNext.getPaint().setFlags(
//                        Paint.UNDERLINE_TEXT_FLAG); // 下划线
//                title_et_stopDisNext.getPaint().setAntiAlias(true); // 抗锯齿
//                title_et_activity_glue_port.getPaint().setFlags(
//                        Paint.UNDERLINE_TEXT_FLAG); // 下划线
//                title_et_activity_glue_port.getPaint().setAntiAlias(true); // 抗锯齿
//
//                title_et_linemid_moveSpeed.setText(pointWeldLineMidParam.getMoveSpeed() + "");
//                title_et_radius.setText(pointWeldLineMidParam.getRadius() + "");
//                title_et_stopDisPrev.setText(pointWeldLineMidParam.getStopGlueDisPrev() + "");
//                title_et_stopDisNext.setText(pointWeldLineMidParam.getStopGLueDisNext() + "");
////					if (pointWeldLineMidParam.isOutGlue()) {
////						title_et_stopDisNext.setText("是");
////					} else {
////						title_et_stopDisNext.setText("否");
////					}
//                title_et_activity_glue_port.setText(GluePort[0] + GluePort[1]
//                        + GluePort[2] + GluePort[3] + GluePort[4]);
//            }
//        }
    }

    /**
     * @Title SetDateAndRefreshUI
     * @Description 打开extendview的时候设置界面内容，显示最新的方案数据而不是没有保存的数据,没有得到保存的方案
     * @author wj
     */
    protected void SetDateAndRefreshUI() {
        weldMidLists = weldMidDao.findAllWeldLineMidParams();
        ArrayList<Integer> list = new ArrayList<>();
        for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
            list.add(pointWeldLineMidParam.get_id());
        }
        System.out.println("存放主键id的集合---->" + list);
        System.out.println("当前选择的方案号---->" + currentTaskNum);
        System.out.println("list是否存在------------》"
                + list.contains(currentTaskNum));
        if (list.contains(currentTaskNum)) {
            // 已经保存在数据库中的数据
            for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                if (currentTaskNum == pointWeldLineMidParam.get_id()) {
                    View extendView = popupListView.getItemViews()
                            .get(currentClickNum).getExtendView();
                    initView(extendView);
                    UpdateInfos(pointWeldLineMidParam);
                }
            }
        } else {
            // 对所有数据进行置空
            View allextendView = popupListView.getItemViews()
                    .get(currentClickNum).getExtendView();
            initView(allextendView);
            UpdateInfos(null);
        }
    }

    protected void save() {
        View extendView = popupListView.getItemViews().get(currentClickNum)
                .getExtendView();
        weldMidLists = weldMidDao.findAllWeldLineMidParams();
        ArrayList<Integer> list = new ArrayList<>();
        for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
            list.add(pointWeldLineMidParam.get_id());
        }
        // 判空
        isOk = isEditClean(extendView);
        if (isOk) {

            PointWeldLineMidParam upLineMidParam = getLineMid(extendView);
            if (weldMidLists.contains(upLineMidParam)) {
                // 默认已经存在的方案但是不能创建方案只能改变默认方案号
                if (list.contains(currentTaskNum)) {
                    isExist = true;
                }
                // 保存的方案已经存在但不是当前编辑的方案
                if (currentTaskNum != weldMidLists.get(
                        weldMidLists.indexOf(upLineMidParam)).get_id()) {
                    ToastUtil.displayPromptInfo(WeldLineMidActivity.this,
                            getResources()
                                    .getString(R.string.task_is_exist_yes));
                }
            } else {

                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                    if (currentTaskNum == pointWeldLineMidParam.get_id()) {// 说明之前插入过
                        flag = true;
                    }
                }
                if (flag) {
                    // 更新数据
                    int rowid = weldMidDao.upDateWeldLineMid(upLineMidParam);
                    // System.out.println("影响的行数"+rowid);
                    update_id.put(upLineMidParam.get_id(), upLineMidParam);
                    // mPMap.map.put(upglueAlone.get_id(), upglueAlone);
                    System.out.println("修改的方案号为：" + upLineMidParam.get_id());
                    // System.out.println(glueAloneDao.getPointGlueAloneParamById(currentTaskNum).toString());
                } else {
                    // 插入一条数据
                    long rowid = weldMidDao.insertWeldLineMid(upLineMidParam);
                    firstExist = true;
                    weldMidLists = weldMidDao.findAllWeldLineMidParams();
                    Log.i(TAG, "保存之后新方案-->" + weldMidLists.toString());
                    ToastUtil.displayPromptInfo(WeldLineMidActivity.this,
                            getResources().getString(R.string.save_success));
                    list.clear();
                    for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                        list.add(pointWeldLineMidParam.get_id());
                    }
                    popupListView.setSelectedEnable(list);
                }
            }
            if (popupListView.isItemZoomIn()) {
                popupListView.zoomOut();
            }
            // 更新title
            refreshTitle();
            flag = false;
        } else {
            ToastUtil.displayPromptInfo(this,
                    getResources().getString(R.string.data_is_null));
        }
    }

    /**
     * @Title refreshTitle
     * @Description 按下保存之后刷新title
     * @author wj
     */
    private void refreshTitle() {
        weldMidLists = weldMidDao.findAllWeldLineMidParams();
        // popupListView->pupupview->title
        for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {

            if (currentTaskNum == pointWeldLineMidParam.get_id()) {
                // 需要设置两个view，因为view内容相同但是parent不同
                View titleViewItem = popupListView.getItemViews()
                        .get(currentClickNum).getPopupView();
                View titleViewExtend = popupListView.getItemViews()
                        .get(currentClickNum).getExtendPopupView();
                setTitleInfos(weldMidLists, titleViewItem, currentTaskNum);
                setTitleInfos(weldMidLists, titleViewExtend, currentTaskNum);
            }
        }
    }

    /**
     * @param extendView
     * @return false表示为空, true表示都有数据
     * @Title isEditClean
     * @Description 判断输入框是否为空
     * @author wj
     */
    private boolean isEditClean(View extendView) {
        et_linemid_moveSpeed = (EditText) extendView.findViewById(R.id.et_linemid_moveSpeed);
        et_sendSnSpeed = (EditText) extendView.findViewById(R.id.et_sendSnSpeed);
        et_stopSnTime = (EditText) extendView.findViewById(R.id.et_stopSnTime);
        if ("".equals(et_linemid_moveSpeed.getText().toString())) {
            return false;
        } else if ("".equals(et_sendSnSpeed.getText().toString())) {
            return false;
        } else if ("".equals(et_stopSnTime.getText().toString())) {
            return false;
        }
        return true;
    }

    /**
     * @param extendView
     * @Title initView
     * @Description 初始化当前extendView视图
     * @author wj
     */
    protected void initView(View extendView) {
        et_linemid_moveSpeed = (EditText) extendView
                .findViewById(R.id.et_linemid_moveSpeed);
        switch_isSn = (ToggleButton) extendView
                .findViewById(R.id.switch_isSn);
        et_sendSnSpeed = (EditText) extendView.findViewById(R.id.et_sendSnSpeed);
        et_stopSnTime = (EditText) extendView.findViewById(R.id.et_stopSnTime);

        tv_moveSpeed = (TextView) extendView.findViewById(R.id.tv_moveSpeed);
        tv_sendSnSpeed = (TextView) extendView.findViewById(R.id.tv_sendSnSpeed);
        extend_save = (TextView) extendView.findViewById(R.id.extend_save);
        extend_default = (TextView) extendView.findViewById(R.id.extend_default);
        extend_mms = (TextView) extendView.findViewById(R.id.extend_mms);
        extend_isSn = (TextView) extendView.findViewById(R.id.extend_isSn);
        tv_radius = (TextView) extendView.findViewById(R.id.tv_radius);
        extend_mm = (TextView) extendView.findViewById(R.id.extend_mm);
        tv_stopSnTime = (TextView) extendView.findViewById(R.id.tv_stopSnTime);
        extend_mm2 = (TextView) extendView.findViewById(R.id.extend_mm2);
        tv_stopDisNext = (TextView) extendView.findViewById(R.id.tv_stopDisNext);
        extend_mm3 = (TextView) extendView.findViewById(R.id.extend_mm3);

		/*===================== begin =====================*/
        et_linemid_moveSpeed.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        extend_save.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        extend_default.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        et_sendSnSpeed.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        et_stopSnTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        tv_moveSpeed.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        tv_sendSnSpeed.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        extend_mms.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        extend_isSn.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        tv_radius.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        extend_mm.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        tv_stopSnTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        extend_mm2.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        tv_stopDisNext.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
        extend_mm3.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(40));
		/*=====================  end =====================*/
    }

    /**
     * 将页面上的数据保存到PointGlueLineMidParam对象中
     *
     * @param extendView
     * @return PointGlueLineMidParam
     */
    private PointWeldLineMidParam getLineMid(View extendView) {
        weldMid = new PointWeldLineMidParam();
        et_linemid_moveSpeed = (EditText) extendView
                .findViewById(R.id.et_linemid_moveSpeed);
        switch_isSn = (ToggleButton) extendView
                .findViewById(R.id.switch_isSn);
        et_sendSnSpeed = (EditText) extendView.findViewById(R.id.et_sendSnSpeed);
        et_stopSnTime = (EditText) extendView.findViewById(R.id.et_stopSnTime);
        try {
            moveSpeedInt = Integer.parseInt(et_linemid_moveSpeed.getText()
                    .toString());

        } catch (NumberFormatException e) {
            moveSpeedInt = 0;
        }
        try {
            sendSnSpeed = Integer.parseInt(et_sendSnSpeed.getText()
                    .toString());

        } catch (NumberFormatException e) {
            sendSnSpeed = 0;
        }
        try {
            stopSnTime = Integer.parseInt(et_stopSnTime.getText()
                    .toString());

        } catch (NumberFormatException e) {
            stopSnTime = 0;
        }

        weldMid.setMoveSpeed(moveSpeedInt);
        weldMid.setSendSnSpeed(sendSnSpeed);
        weldMid.setStopSnTime(stopSnTime);
        weldMid.setSn(switch_isSn.isChecked());

        weldMid.set_id(currentTaskNum);

        return weldMid;
    }

    @Override
    public void onBackPressed() {
        // 不想保存只想回退，不保存数据
        if (popupListView.isItemZoomIn()) {
            popupListView.zoomOut();
        } else {
            complete();
            super.onBackPressed();
            overridePendingTransition(R.anim.in_from_left,
                    R.anim.out_from_right);
        }
    }

    private void complete() {
        ArrayList<? extends PopupView> itemPopuViews = popupListView
                .getItemViews();
        for (PopupView popupView : itemPopuViews) {
            ImageView iv_selected = (ImageView) popupView.getPopupView()
                    .findViewById(R.id.iv_selected);
            if (iv_selected.getVisibility() == View.VISIBLE) {
                mIndex = itemPopuViews.indexOf(popupView) + 1;
            }
        }
        System.out.println("返回的方案号为================》" + mIndex);
        point.setPointParam(weldMidDao.getPointWeldLineMidParam(mIndex));
        System.out.println("返回的Point为================》" + point);

        List<Map<Integer, PointWeldLineMidParam>> list = new ArrayList<Map<Integer, PointWeldLineMidParam>>();
        list.add(update_id);
        Log.i(TAG, point.toString());
        Bundle extras = new Bundle();
        extras.putParcelable(MyPopWindowClickListener.POPWINDOW_KEY, point);
        extras.putInt(MyPopWindowClickListener.FLAG_KEY, mFlag);
        // 须定义一个list用于在budnle中传递需要传递的ArrayList<Object>,这个是必须要的
        ArrayList bundlelist = new ArrayList();
        bundlelist.add(list);
        extras.putParcelableArrayList(MyPopWindowClickListener.TYPE_UPDATE,
                bundlelist);
        intent.putExtras(extras);

        setResult(TaskActivity.resultCode, intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:// 返回按钮的响应事件
                if (popupListView.isItemZoomIn()) {
                    popupListView.zoomOut();
                } else {
                    complete();
                    super.onBackPressed();
                    overridePendingTransition(R.anim.in_from_left,
                            R.anim.out_from_right);
                }
                break;
            default:
                break;
        }
    }
    private class RevHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what== SocketInputThread.SocketError){
                //wifi中断
                System.out.println("wifi连接断开。。");
                SocketThreadManager.releaseInstance();
                System.out.println("单例被释放了-----------------------------");
                //设置全局变量，跟新ui
                userApplication.setWifiConnecting(false);
//				WifiConnectTools.processWifiConnect(userApplication, iv_wifi_connecting);
                ToastUtil.displayPromptInfo(WeldLineMidActivity.this,"wifi连接断开。。");
            } else if (msg.what == Activity_Init_View) {
                View activity_glue_popuplistview = stub_glue.inflate();
                popupListView = (PopupListView) activity_glue_popuplistview.findViewById(R.id.popupListView);
                popupListView.init(null);
                CreatePopupViews();
            }
        }
    }

    private void CreatePopupViews() {
        // 初始化创建10个popupView
        for (int i = 0; i < 10; i++) {
            p = i + 1;
            PopupView popupView = new PopupView(this, R.layout.popup_view_item_glue_mid) {

                @Override
                public void setViewsElements(View view) {
//					TextView textView = (TextView) view
//							.findViewById(R.id.title);
                    weldMidLists = weldMidDao.findAllWeldLineMidParams();
                    ImageView title_num = (ImageView) view
                            .findViewById(R.id.title_num);
                    if (p == 1) {// 方案列表第一位对应一号方案
                        title_num.setImageResource(R.drawable.green1);
//                        setTitleInfos(weldMidLists, view, p);
                    } else if (p == 2) {
                        title_num.setImageResource(R.drawable.green2);
//                        setTitleInfos(weldMidLists, view, p);
                    } else if (p == 3) {
                        title_num.setImageResource(R.drawable.green3);
//                        setTitleInfos(weldMidLists, view, p);
                    } else if (p == 4) {
                        title_num.setImageResource(R.drawable.green4);
//                        setTitleInfos(weldMidLists, view, p);
                    } else if (p == 5) {
                        title_num.setImageResource(R.drawable.green5);
//                        setTitleInfos(weldMidLists, view, p);
                    } else if (p == 6) {
                        title_num.setImageResource(R.drawable.green6);
//                        setTitleInfos(weldMidLists, view, p);
                    } else if (p == 7) {
                        title_num.setImageResource(R.drawable.green7);
//                        setTitleInfos(weldMidLists, view, p);
                    } else if (p == 8) {
                        title_num.setImageResource(R.drawable.green8);
//                        setTitleInfos(weldMidLists, view, p);
                    } else if (p == 9) {
                        title_num.setImageResource(R.drawable.green9);
//                        setTitleInfos(weldMidLists, view, p);
                    } else if (p == 10) {
                        title_num.setImageResource(R.drawable.green10);
//                        setTitleInfos(weldMidLists, view, p);
                    }
                }

                @Override
                public View setExtendView(View view) {
                    if (view == null) {
                        extendView = LayoutInflater.from(
                                getApplicationContext()).inflate(
                                R.layout.glue_mid_extend_view, null);
                        int size = weldMidLists.size();
                        while (size > 0) {
                            size--;
                            if (p == 1) {// 方案列表第一位对应一号方案
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            } else if (p == 2) {
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            } else if (p == 3) {
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            } else if (p == 4) {
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            } else if (p == 5) {
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            } else if (p == 6) {
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            } else if (p == 7) {
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            } else if (p == 8) {
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            } else if (p == 9) {
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            } else if (p == 10) {
                                initView(extendView);
                                for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
                                    if (p == pointWeldLineMidParam.get_id()) {
                                        UpdateInfos(pointWeldLineMidParam);
                                    }
                                }
                            }
                        }
                        extendView.setBackgroundColor(Color.WHITE);
                    } else {
                        extendView = view;
                    }
                    return extendView;
                }

                @Override
                public void initViewAndListener(View extendView) {
                    et_linemid_moveSpeed = (EditText) extendView
                            .findViewById(R.id.et_linemid_moveSpeed);
                    switch_isSn = (ToggleButton) extendView
                            .findViewById(R.id.switch_isSn);
                    et_sendSnSpeed = (EditText) extendView
                            .findViewById(R.id.et_sendSnSpeed);
                    et_stopSnTime = (EditText) extendView
                            .findViewById(R.id.et_stopSnTime);


                    // 轨迹速度设置最大最小值
                    et_linemid_moveSpeed
                            .addTextChangedListener(new MaxMinEditWatcher(
                                    PointConfigParam.GlueLineMid.MoveSpeed,
                                    PointConfigParam.GlueLineMid.GlueLineMidMin,
                                    et_linemid_moveSpeed));
                    et_linemid_moveSpeed
                            .setOnFocusChangeListener(new MaxMinFocusChangeListener(
                                    PointConfigParam.GlueLineMid.MoveSpeed,
                                    PointConfigParam.GlueLineMid.GlueLineMidMin,
                                    et_linemid_moveSpeed));
                    et_linemid_moveSpeed.setSelectAllOnFocus(true);
                    // 轨迹速度设置最大最小值
                    et_sendSnSpeed
                            .addTextChangedListener(new MaxMinEditWatcher(
                                    PointConfigParam.GlueLineMid.MoveSpeed,
                                    PointConfigParam.GlueLineMid.GlueLineMidMin,
                                    et_sendSnSpeed));
                    et_sendSnSpeed
                            .setOnFocusChangeListener(new MaxMinFocusChangeListener(
                                    PointConfigParam.GlueLineMid.MoveSpeed,
                                    PointConfigParam.GlueLineMid.GlueLineMidMin,
                                    et_sendSnSpeed));
                    et_sendSnSpeed.setSelectAllOnFocus(true);
                    et_stopSnTime
                            .addTextChangedListener(new MaxMinEditWatcher(
                                    PointConfigParam.GlueLineMid.MoveSpeed,
                                    PointConfigParam.GlueLineMid.GlueLineMidMin,
                                    et_stopSnTime));
                    et_stopSnTime
                            .setOnFocusChangeListener(new MaxMinFocusChangeListener(
                                    PointConfigParam.GlueLineMid.MoveSpeed,
                                    PointConfigParam.GlueLineMid.GlueLineMidMin,
                                    et_stopSnTime));
                    et_stopSnTime.setSelectAllOnFocus(true);

                    rl_moren = (RelativeLayout) extendView
                            .findViewById(R.id.rl_moren);
                    iv_add = (ImageView) extendView.findViewById(R.id.iv_add);
                    rl_save = (RelativeLayout) extendView
                            .findViewById(R.id.rl_save);// 保存按钮
                    iv_moren = (ImageView) extendView
                            .findViewById(R.id.iv_moren);// 默认按钮
                    rl_moren.setOnClickListener(this);
                    rl_save.setOnClickListener(this);
                    // 点击全选
                }

                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.rl_moren:// 设为默认
                            // 判断界面
                            save();
                            if ((isOk && isExist) || firstExist) {// 不为空且已经存在或者不存在且插入新的
                                // 刷新ui
                                mMorenTextView.setText("当前默认方案号(" + currentTaskNum
                                        + ")");
                                // 默认号存到sp
                                SharePreferenceUtils
                                        .saveParamNumberToPref(
                                                WeldLineMidActivity.this,
                                                SettingParam.DefaultNum.ParamGlueLineMidNumber,
                                                currentTaskNum);
                            }
                            isExist = false;
                            firstExist = false;
                            // 更新数据
                            break;
                        case R.id.rl_save:// 保存
                            save();
                            // 数据库保存数据
                            break;

                        default:
                            break;
                    }
                }
            };
            popupViews.add(popupView);
        }
        popupListView.setItemViews(popupViews);
        if (mType != 1) {
            popupListView.setPosition(defaultNum - 1);// 第一次默认选中第一个item，后面根据方案号(新建点)
        } else {
            // 显示point的参数方案
            // PointGlueAloneParam glueAloneParam= (PointGlueAloneParam)
            // point.getPointParam();
            // System.out.println("传进来的方案号为----------》"+glueAloneParam.get_id());
            popupListView.setPosition(point.getPointParam().get_id() - 1);
        }
        ArrayList<Integer> list = new ArrayList<>();
        for (PointWeldLineMidParam pointWeldLineMidParam : weldMidLists) {
            list.add(pointWeldLineMidParam.get_id());
        }
        popupListView.setSelectedEnable(list);
        popupListView.setOnClickPositionChanged(new PopupListView.OnClickPositionChanged() {
            @Override
            public void getCurrentPositon(int position) {
                currentTaskNum = position + 1;
                currentClickNum = position;
            }
        });
        popupListView.setOnZoomInListener(new PopupListView.OnZoomInChanged() {

            @Override
            public void getZoomState(Boolean isZoomIn) {
                if (isZoomIn) {
                    // 设置界面
                    SetDateAndRefreshUI();
                }
            }
        });
        rl_back.setOnClickListener(this);
        iv_loading.setVisibility(View.INVISIBLE);

    }

}
