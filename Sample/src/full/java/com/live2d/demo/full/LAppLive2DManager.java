/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.live2d.demo.full;

import com.live2d.sdk.cubism.framework.math.CubismMatrix44;
import com.live2d.sdk.cubism.framework.motion.ACubismMotion;
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback;
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.res.AssetManager;

import static com.live2d.demo.LAppDefine.*;

/**
 * 샘플 애플리케이션에서 Cubism Model을 관리하는 클래스.
 * * 모델 생성과 파기, 탭 이벤트 처리, 모델 전환을 실시한다.
 * */
public class LAppLive2DManager {
    public static LAppLive2DManager getInstance() {
        if (s_instance == null) {
            s_instance = new LAppLive2DManager();
        }
        return s_instance;
    }

    public static void releaseInstance() {
        s_instance = null;
    }

    /**
     * 현재 장면에서 보유하고 있는 모든 모델을 해방
     */
    public void releaseAllModel() {
        for (LAppModel model : models) {
            model.deleteModel();
        }
        models.clear();
    }

    /**
     * assets 폴더에 있는 모델 폴더 이름을 설정하다
     */
    public void setUpModel() {
        // assets 폴더 안에 있는 폴더명을 모두 크롤하고 모델이 존재하는 폴더를 정의한다.
        // 폴더는 있으나 동명의 .model 3.json이 발견되지 않은 경우 목록에 포함하지 않는다.
        modelDir.clear();

        final AssetManager assets = LAppDelegate.getInstance().getActivity().getResources().getAssets();
        try {
            String[] root = assets.list("");
            for (String subdir: root) {
                String[] files = assets.list(subdir);
                String target = subdir + ".model3.json";
                // 폴더와 동명의 .model3.json이 있는지 탐색하다
                for (String file : files) {
                    if (file.equals(target)) {
                        modelDir.add(subdir);
                        break;
                    }
                }
            }
            Collections.sort(modelDir);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    // 모델 갱신 처리 및 그리기 처리를 수행한다
    public void onUpdate() {
        int width = LAppDelegate.getInstance().getWindowWidth();
        int height = LAppDelegate.getInstance().getWindowHeight();

        for (int i = 0; i < models.size(); i++) {
            LAppModel model = models.get(i);

            if (model.getModel() == null) {
                LAppPal.printLog("Failed to model.getModel().");
                continue;
            }

            projection.loadIdentity();

            if (model.getModel().getCanvasWidth() > 1.0f && width < height) {
                // 가로로 긴 모델을 세로로 긴 창에 표시할 때 모델의 가로 크기로 scale을 산출한다
                model.getModelMatrix().setWidth(2.0f);
                projection.scale(1.0f, (float) width / (float) height);
            } else {
                projection.scale((float) height / (float) width, 1.0f);
            }

            // 필요가 있으면 여기서 곱한다
            if (viewMatrix != null) {
                viewMatrix.multiplyByMatrix(projection);
            }

            // モデル1体描画前コール
            LAppDelegate.getInstance().getView().preModelDraw(model);

            model.update();

            model.draw(projection);     // 참조도이므로 projection은 변질된다

            // モデル1体描画後コール
            LAppDelegate.getInstance().getView().postModelDraw(model);
        }
    }

    /**
     * 화면을 드래그했을 때의 처리
     *
     * @param x 画面のx座標
     * @param y 画面のy座標
     */
    public void onDrag(float x, float y) {
        for (int i = 0; i < models.size(); i++) {
            LAppModel model = getModel(i);
            model.setDragging(x, y);
        }
    }

    /**
     * 화면을 탭했을 때의 처리
     *
     * @param x 画面のx座標
     * @param y 画面のy座標
     */
    public void onTap(float x, float y) {
        if (DEBUG_LOG_ENABLE) {
            LAppPal.printLog("tap point: {" + x + ", y: " + y);
        }

        for (int i = 0; i < models.size(); i++) {
            LAppModel model = models.get(i);

            // 머리를 터치했을 경우 표정을 랜덤으로 재생하다
            if (model.hitTest(HitAreaName.BODY.getId(), x, y)) {
                if (DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("hit area: " + HitAreaName.BODY.getId());
                }
                model.startRandomMotion(MotionGroup.TAP_BODY.getId(), Priority.NORMAL.getPriority(), finishedMotion, beganMotion);
            }
            // model.setRandomExpression();
        }

    }

    /**
     * 다음 장면으로 전환하다
     * 샘플 애플리케이션에서는 모델 세트를 전환한다
     */
    public void nextScene() {
        final int number = (currentModel + 1) % modelDir.size();

        changeScene(number);
    }

    /**
     * 장면을 바꾸다
     *
     * @param index 전환 장면 지수
     */
    public void changeScene(int index) {
        currentModel = index;
        if (DEBUG_LOG_ENABLE) {
            LAppPal.printLog("model index: " + currentModel);
        }

        String modelDirName = modelDir.get(index);

        String modelPath = ResourcePath.ROOT.getPath() + modelDirName + "/";
        String modelJsonName = modelDirName + ".model3.json";

        releaseAllModel();

        models.add(new LAppModel());
        models.get(0).loadAssets(modelPath, modelJsonName);

        /*
         * 모델 반투명 표시를 하는 샘플을 제시한다.
         * 여기서 USE_RENDER_TARGET, USE_MODEL_RENDER_TARGET이 정의되어 있는 경우
         * 다른 렌더링 타겟에 모델을 그려, 그리기 결과를 텍스처로서 다른 스프라이트에 붙인다.
         */
        LAppView.RenderingTarget useRenderingTarget;
        if (USE_RENDER_TARGET) {
            // LappView가 가진 타겟에 그림을 그릴 경우 이를 선택
            useRenderingTarget = LAppView.RenderingTarget.VIEW_FRAME_BUFFER;
        } else if (USE_MODEL_RENDER_TARGET) {
            // 각 Lapp Model이 가진 타겟에 그림을 그릴 경우 이를 선택
            useRenderingTarget = LAppView.RenderingTarget.MODEL_FRAME_BUFFER;
        } else {
            // 기본 메인프레임 버퍼로 렌더링(일반)
            useRenderingTarget = LAppView.RenderingTarget.NONE;
        }

        if (USE_RENDER_TARGET || USE_MODEL_RENDER_TARGET) {
            // 모델 개별적으로 α를 붙이는 샘플로서 또 하나의 모델을 작성해 조금 위치를 바꾼다.
            models.add(new LAppModel());
            models.get(1).loadAssets(modelPath, modelJsonName);
            models.get(1).getModelMatrix().translateX(0.2f);
        }

        // 렌더링 타겟을 전환하다
        LAppDelegate.getInstance().getView().switchRenderingTarget(useRenderingTarget);

        // 별도 렌더링 대상 선택 시 배경 클리어 색상
        float[] clearColor = {0.0f, 0.0f, 0.0f};
        LAppDelegate.getInstance().getView().setRenderingTargetClearColor(clearColor[0], clearColor[1], clearColor[2]);
    }

    /**
     * 현재 장면에서 보유하고 있는 모델을 반환한다
     *
     * @param number 모델 목록의 인덱스 값
     * @return 모델의 인스턴스를 반환한다. 인덱스 값이 범위를 벗어난 경우 null을 반환한다
     */
    public LAppModel getModel(int number) {
        if (number < models.size()) {
            return models.get(number);
        }
        return null;
    }

    /**
     * 장면 지수를 반환하다
     *
     * @return 씬 인덱스
     */
    public int getCurrentModel() {
        return currentModel;
    }

    /**
     * Return the number of models in this LAppLive2DManager instance has.
     *
     * @return number fo models in this LAppLive2DManager instance has. If models list is null, return 0.
     */
    public int getModelNum() {
        if (models == null) {
            return 0;
        }
        return models.size();
    }

    /**
     * 모션 재생 시 실행되는 콜백 함수
     */
    private static class BeganMotion implements IBeganMotionCallback {
        @Override
        public void execute(ACubismMotion motion) {
            LAppPal.printLog("Motion Began: " + motion);
        }
    }

    private static final BeganMotion beganMotion = new BeganMotion();

    /**
     * 모션 종료 시 실행되는 콜백 함수
     */
    private static class FinishedMotion implements IFinishedMotionCallback {
        @Override
        public void execute(ACubismMotion motion) {
            LAppPal.printLog("Motion Finished: " + motion);
        }
    }

    private static final FinishedMotion finishedMotion = new FinishedMotion();

    /**
     * 싱글턴 인스턴스
     */
    private static LAppLive2DManager s_instance;

    private LAppLive2DManager() {
        setUpModel();
        changeScene(0);
    }

    private final List<LAppModel> models = new ArrayList<>();

    /**
     * 표시할 장면의 인덱스 값
     */
    private int currentModel;

    /**
     * 모델 디렉토리 이름
     */
    private final List<String> modelDir = new ArrayList<>();

    // onUpdate메소드에서사용되는캐시변수
    private final CubismMatrix44 viewMatrix = CubismMatrix44.create();
    private final CubismMatrix44 projection = CubismMatrix44.create();
}
