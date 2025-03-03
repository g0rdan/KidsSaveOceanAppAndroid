package com.kidssaveocean.fatechanger.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kidssaveocean.fatechanger.R
import android.graphics.drawable.Drawable

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.kidssaveocean.fatechanger.extensions.getScreenSize
import android.graphics.*
import android.os.AsyncTask
import android.widget.ImageView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_main_dashboard.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.kidssaveocean.fatechanger.bottomNavigation.BottomNavigationActivity
import com.kidssaveocean.fatechanger.countryContacts.CountryIntroFragment
import com.kidssaveocean.fatechanger.database.AppDatabase
import com.kidssaveocean.fatechanger.database.entities.DashboardStep
import com.kidssaveocean.fatechanger.database.entities.KeyValue
import com.kidssaveocean.fatechanger.extensions.setImage
import com.kidssaveocean.fatechanger.extensions.addToNavigationStack
import com.kidssaveocean.fatechanger.views.ActionAlertDialog
import io.reactivex.subjects.PublishSubject
import android.media.MediaPlayer
import android.content.res.AssetFileDescriptor
import com.kidssaveocean.fatechanger.policy.PolicyHomeActivity
import java.io.IOException


class MainDashboardFragment (step: DashboardSteps? = null) : Fragment() {

    private var currentCircleAngle : Float = 0f
    private var currentMeteorAngle : Float = 0f
    private var topLeftSteeringWeelCorner : PointF? = null
    private var bottomLeftPoint : PointF? = null
    private var lastStep : String? = null

    private var stepOne : DashboardTask = DashboardTask(DashboardSteps.RESEARCH)
    private var stepTwo : DashboardTask = DashboardTask(DashboardSteps.WRITE_LETTER)
    private var stepThree : DashboardTask = DashboardTask(DashboardSteps.SHARING)
    private var stepFour : DashboardTask = DashboardTask(DashboardSteps.LETTER_CAMPAING)
    private var stepFive : DashboardTask = DashboardTask(DashboardSteps.GOVERNMENT)
    private var stepSix : DashboardTask = DashboardTask(DashboardSteps.PROTEST)
    private var stepSeven : DashboardTask = DashboardTask(DashboardSteps.HIJACK)

    private var currentTask : DashboardTask = DashboardTask(DashboardSteps.RESEARCH)
    private var db : AppDatabase? = null
    val mp = MediaPlayer()

    val isAllPointsInitiated : Boolean
        get() {
            if (bottomLeftPoint == null) return false
            if (topLeftSteeringWeelCorner == null) return false
            return true
        }

    init {
        stepTwo.isSecondCompleted = false
        step?.let {
            when(it) {
                DashboardSteps.RESEARCH -> lastStep = DashboardStep.STEP_1
                DashboardSteps.WRITE_LETTER -> lastStep = DashboardStep.STEP_2
                DashboardSteps.SHARING -> lastStep = DashboardStep.STEP_3
                DashboardSteps.LETTER_CAMPAING -> lastStep = DashboardStep.STEP_4
                DashboardSteps.GOVERNMENT -> lastStep = DashboardStep.STEP_5
                DashboardSteps.PROTEST -> lastStep = DashboardStep.STEP_6
                DashboardSteps.HIJACK -> lastStep = DashboardStep.STEP_7
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_dashboard, container, false)
    }

    override fun onResume() {
        super.onResume()
        initDashboardSteps(activity as BottomNavigationActivity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomActivity = activity as BottomNavigationActivity

        initDashboardSteps(bottomActivity)

        currentTask?.completedObservable?.subscribe {

            bottomActivity?.runOnUiThread {
                when (currentTask.type) {
                    DashboardSteps.RESEARCH -> {
                        stepOne.isFirstCompleted = it.first
                        stepOne.isSecondCompleted = it.second
                        changeCompletedStatus(bottomActivity, stepOne)
                    }
                    DashboardSteps.WRITE_LETTER -> {
                        stepTwo.isFirstCompleted = it.first
                        stepTwo.isSecondCompleted = it.second
                        changeCompletedStatus(bottomActivity, stepTwo)
                    }
                    DashboardSteps.SHARING -> {
                        stepThree.isFirstCompleted = it.first
                        stepThree.isSecondCompleted = it.second
                        changeCompletedStatus(bottomActivity, stepThree)
                    }
                    DashboardSteps.LETTER_CAMPAING -> {
                        stepFour.isFirstCompleted = it.first
                        stepFour.isSecondCompleted = it.second
                        changeCompletedStatus(bottomActivity, stepFour)
                    }
                    DashboardSteps.GOVERNMENT -> {
                        stepFive.isFirstCompleted = it.first
                        stepFive.isSecondCompleted = it.second
                        changeCompletedStatus(bottomActivity, stepFive)
                    }
                    DashboardSteps.PROTEST -> {
                        stepSix.isFirstCompleted = it.first
                        stepSix.isSecondCompleted = it.second
                        changeCompletedStatus(bottomActivity, stepSix)
                    }
                    DashboardSteps.HIJACK -> {
                        stepSeven.isFirstCompleted = it.first
                        stepSeven.isSecondCompleted = it.second
                        changeCompletedStatus(bottomActivity, stepSeven)
                    }
                }
            }
        }

        action_alert_button.setOnClickListener {
            ActionAlertDialog(bottomActivity).show()
        }

        firstIcon.setOnClickListener {
            playClick(bottomActivity)
            clickOnSurfer(stepOne)
        }

        secondIcon.setOnClickListener {
            playClick(bottomActivity)
            clickOnSurfer(stepTwo)
        }

        thirdIcon.setOnClickListener {
            playClick(bottomActivity)
            clickOnSurfer(stepThree)
        }

        fourthIcon.setOnClickListener {
            playClick(bottomActivity)
            clickOnSurfer(stepFour)
        }

        fifthIcon.setOnClickListener {
            playClick(bottomActivity)
            clickOnSurfer(stepFive)
        }

        sixthIcon.setOnClickListener {
            playClick(bottomActivity)
            clickOnSurfer(stepSix)
        }

        seventhIcon.setOnClickListener {
            playClick(bottomActivity)
            clickOnSurfer(stepSeven)
        }

        how_button?.setOnClickListener {
            howButtonAction(currentTask.type!!)
        }

        I_did_it_1?.setOnClickListener {
            if (currentTask.type == DashboardSteps.WRITE_LETTER) {
                currentTask.isSecondCompleted = currentTask.isSecondCompleted.not()
                saveTaskToDb(currentTask)
            }
        }

        I_did_it_2?.setOnClickListener {
            currentTask.isFirstCompleted = currentTask.isFirstCompleted.not()
            saveTaskToDb(currentTask)
        }

        icons.viewTreeObserver.addOnGlobalLayoutListener {
            if (icons != null) {
                bottomLeftPoint = PointF(icons.x, icons.y + icons.height)
                if (isAllPointsInitiated) {
                    view.background = BackgroundDrawable(
                            activity as Activity,
                            bottomLeftPoint!!,
                            topLeftSteeringWeelCorner!!)
                }
            }
        }

        alert_field.viewTreeObserver.addOnGlobalLayoutListener {
            if (alert_field != null) {
                topLeftSteeringWeelCorner = PointF(alert_field.x, alert_field.y)
                if (isAllPointsInitiated) {
                    view.background = BackgroundDrawable(
                            activity as Activity,
                            bottomLeftPoint!!,
                            topLeftSteeringWeelCorner!!)
                }
            }
        }
    }

    private fun clickOnSurfer(step : DashboardTask) {
        saveLastCurrentTaskToDb(step)
        currentTask.type = step.type
        currentTask.isFirstCompleted = step.isFirstCompleted
        currentTask.isSecondCompleted = step.isSecondCompleted
        changeVisuals(currentTask)
    }

    private fun saveTaskToDb(task : DashboardTask) {
        AsyncTask.execute {
            val step = db?.dashboardStepDao()?.getDashboardStep(task.type!!)
            val newStep = DashboardStep(task.type!!, task.isFirstCompleted, task.isSecondCompleted)
            when (step) {
                null -> db?.dashboardStepDao()?.insertDashboardStep(newStep)
                else -> db?.dashboardStepDao()?.updateDashboardStep(newStep)
            }
        }
    }

    private fun saveLastCurrentTaskToDb(task : DashboardTask) {
        var value = ""
        when(task.type) {
            DashboardSteps.RESEARCH -> value = DashboardStep.STEP_1
            DashboardSteps.WRITE_LETTER -> value = DashboardStep.STEP_2
            DashboardSteps.SHARING -> value = DashboardStep.STEP_3
            DashboardSteps.LETTER_CAMPAING -> value = DashboardStep.STEP_4
            DashboardSteps.GOVERNMENT -> value = DashboardStep.STEP_5
            DashboardSteps.PROTEST -> value = DashboardStep.STEP_6
            DashboardSteps.HIJACK -> value = DashboardStep.STEP_7
        }

        AsyncTask.execute {
            val keyValue = db?.keyValueDao()?.getKeyValue(KeyValue.LAST_CURRENT_STEP)
            val newKeyValue = KeyValue(KeyValue.LAST_CURRENT_STEP, value)
            when (keyValue) {
                null -> db?.keyValueDao()?.insertKeyValue(newKeyValue)
                else -> db?.keyValueDao()?.updateKeyValue(newKeyValue)
            }
        }
    }

    private fun changeVisuals(task : DashboardTask) {
        activity?.runOnUiThread {
            firstIcon.isActive = task.type?.ordinal == 0
            secondIcon.isActive = task.type?.ordinal == 1
            thirdIcon.isActive = task.type?.ordinal == 2
            fourthIcon.isActive = task.type?.ordinal == 3
            fifthIcon.isActive = task.type?.ordinal == 4
            sixthIcon.isActive = task.type?.ordinal == 5
            seventhIcon.isActive = task.type?.ordinal == 6
            rotateWheel(task.type!!)
            rotateMeteor(task.type!!)
            changeDescriptionText(task)
            changeCompletedStatus(activity as BottomNavigationActivity, task)
        }
    }

    private fun rotateWheel(step : DashboardSteps) {
        when (step.ordinal) {
            0,1,2,3,4,5,6 -> {
                val angleStep = 42.5f
                val newAngle = step.ordinal * angleStep
                val rotate = RotateAnimation(
                        currentCircleAngle,
                        newAngle,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                )
                rotate.fillAfter = true
                rotate.duration = 1000
                floating_area.startAnimation(rotate)
                currentCircleAngle = newAngle
            }
            else -> println("Wrong position number")
        }
    }

    private fun rotateMeteor(step : DashboardSteps) {
        when (step.ordinal) {
            0,1,2,3,4,5,6 -> {
                val angleStep = 25f
                val newAngle = (step.ordinal + 1) * angleStep
                val rotate = RotateAnimation(
                        currentMeteorAngle,
                        newAngle,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                )
                rotate.fillAfter = true
                rotate.duration = 1000
                meteor_ball.startAnimation(rotate)
                currentMeteorAngle = newAngle
            }
            else -> println("Wrong position number")
        }
    }

    private fun changeCompletedStatus(context : Context, task : DashboardTask) {
        when (currentTask.type) {
            DashboardSteps.RESEARCH -> {
                firstIcon.isCompleted = task.isFirstCompleted
                I_did_it_1.visibility = View.GONE
                changeTextOnButtonAndFistIcon(context, task.isFirstCompleted)
            }
            DashboardSteps.WRITE_LETTER -> {
                secondIcon.isCompleted = task.isFirstCompleted && task.isSecondCompleted
                I_did_it_1.visibility = View.VISIBLE
                when (secondIcon.isCompleted) {
                    true -> complete_image.setImage(context, R.drawable.complete_fist_and_writing)
                    false -> complete_image.setImage(context, R.drawable.incomplete_fist_and_writing)
                }
                when (task.isSecondCompleted) {
                    true -> I_did_it_1.text = getString(R.string.Not_yet)
                    false -> I_did_it_1.text = getString(R.string.I_did_it_about_plastic)
                }
                when (task.isFirstCompleted) {
                    true -> I_did_it_2.text = getString(R.string.Not_yet)
                    false -> I_did_it_2.text = getString(R.string.I_did_it_about_climate)
                }
            }
            DashboardSteps.SHARING -> {
                thirdIcon.isCompleted = task.isFirstCompleted
                I_did_it_1.visibility = View.GONE
                changeTextOnButtonAndFistIcon(context, task.isFirstCompleted)
            }
            DashboardSteps.LETTER_CAMPAING -> {
                fourthIcon.isCompleted = task.isFirstCompleted
                I_did_it_1.visibility = View.GONE
                changeTextOnButtonAndFistIcon(context, task.isFirstCompleted)
            }
            DashboardSteps.GOVERNMENT -> {
                fifthIcon.isCompleted = task.isFirstCompleted
                I_did_it_1.visibility = View.GONE
                changeTextOnButtonAndFistIcon(context, task.isFirstCompleted)
            }
            DashboardSteps.PROTEST -> {
                sixthIcon.isCompleted = task.isFirstCompleted
                I_did_it_1.visibility = View.GONE
                changeTextOnButtonAndFistIcon(context, task.isFirstCompleted)
            }
            DashboardSteps.HIJACK -> {
                seventhIcon.isCompleted = task.isFirstCompleted
                I_did_it_1.visibility = View.GONE
                when(task.isFirstCompleted) {
                    true -> {
                        complete_image.setImage(context, R.drawable.complete_fist_and_writing)
                        I_did_it_2.text = getString(R.string.Not_yet)
                    }
                    false -> {
                        complete_image.setImage(context, R.drawable.incomplete_fist_and_writing)
                        I_did_it_2.text = getString(R.string.I_plan_to)
                    }
                }
            }
            else -> println("Unknown type for DashboardSteps")
        }
    }

    private fun changeTextOnButtonAndFistIcon(context: Context, completed : Boolean) {
        when(completed) {
            true -> {
                complete_image.setImage(context, R.drawable.complete_fist_and_writing)
                I_did_it_2.text = getString(R.string.Not_yet)
            }
            false -> {
                complete_image.setImage(context, R.drawable.incomplete_fist_and_writing)
                I_did_it_2.text = getString(R.string.I_did_it)
            }
        }
    }

    private fun changeIconsBg(task : DashboardTask) {
        when (task.type) {
            DashboardSteps.RESEARCH -> firstIcon.isCompleted = task.isFirstCompleted
            DashboardSteps.WRITE_LETTER -> secondIcon.isCompleted = task.isFirstCompleted && task.isSecondCompleted
            DashboardSteps.SHARING -> thirdIcon.isCompleted = task.isFirstCompleted
            DashboardSteps.LETTER_CAMPAING -> fourthIcon.isCompleted = task.isFirstCompleted
            DashboardSteps.GOVERNMENT -> fifthIcon.isCompleted = task.isFirstCompleted
            DashboardSteps.PROTEST -> sixthIcon.isCompleted = task.isFirstCompleted
            DashboardSteps.HIJACK -> seventhIcon.isCompleted = task.isFirstCompleted
            else -> println("Unknown type for DashboardSteps")
        }
    }

    private fun changeDescriptionText(task : DashboardTask) {
        when (task.type) {
            DashboardSteps.RESEARCH -> task_description.text = getString(R.string.research_task_description)
            DashboardSteps.WRITE_LETTER -> task_description.text = getString(R.string.write_letter_task_description)
            DashboardSteps.SHARING -> task_description.text = getString(R.string.sharing_task_description)
            DashboardSteps.LETTER_CAMPAING -> task_description.text = getString(R.string.letter_campaing_task_description)
            DashboardSteps.GOVERNMENT -> task_description.text = getString(R.string.government_task_description)
            DashboardSteps.PROTEST -> task_description.text = getString(R.string.protest_task_description)
            DashboardSteps.HIJACK -> task_description.text = getString(R.string.env_policy_description)
            else -> println("Unknown type for DashboardSteps")
        }
    }

    private fun howButtonAction(step : DashboardSteps) {
        val bottomActivity = activity as BottomNavigationActivity
        when (step) {
            DashboardSteps.RESEARCH -> bottomActivity.setMenuItem(R.id.action_resources)
            DashboardSteps.WRITE_LETTER -> {
                CountryIntroFragment().addToNavigationStack(
                        bottomActivity.supportFragmentManager,
                        R.id.fragment_container,
                        "country_fragment")
            }
            DashboardSteps.SHARING -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "https://www.kidssaveocean.com/")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)

            }
            DashboardSteps.LETTER_CAMPAING -> {
                StartActivistCampaignFragment().addToNavigationStack(
                        bottomActivity.supportFragmentManager,
                        R.id.fragment_container,
                        "activist_fragment")
            }
            DashboardSteps.GOVERNMENT -> {
                EngageLocalGovernmentFragment().addToNavigationStack(
                        bottomActivity.supportFragmentManager,
                        R.id.fragment_container,
                        "government_fragment")
            }
            DashboardSteps.PROTEST -> {
                NobodysListeningFragment().addToNavigationStack(
                        bottomActivity.supportFragmentManager,
                        R.id.fragment_container,
                        "nobody_listening_fragment")
            }
            DashboardSteps.HIJACK -> {
                startActivity(Intent(activity, PolicyHomeActivity::class.java))
            }
        }
    }

    private fun initDashboardSteps(context: Context){
        if (db == null) {
            db = AppDatabase.getAppDatabase(context)
        }

        db?.let {
            AsyncTask.execute {
                val savedSteps = it.dashboardStepDao().getAllDashboardSteps()
                savedSteps.forEach{
                    when(it.step){
                        DashboardSteps.RESEARCH -> stepOne.isFirstCompleted = it.first_completed_step
                        DashboardSteps.WRITE_LETTER -> {
                            stepTwo.isFirstCompleted = it.first_completed_step
                            stepTwo.isSecondCompleted = it.second_completed_step
                        }
                        DashboardSteps.SHARING -> stepThree.isFirstCompleted = it.first_completed_step
                        DashboardSteps.LETTER_CAMPAING -> stepFour.isFirstCompleted = it.first_completed_step
                        DashboardSteps.GOVERNMENT -> stepFive.isFirstCompleted = it.first_completed_step
                        DashboardSteps.PROTEST -> stepSix.isFirstCompleted = it.first_completed_step
                        DashboardSteps.HIJACK -> stepSeven.isFirstCompleted = it.first_completed_step
                    }
                }

                changeIconsBg(stepOne)
                changeIconsBg(stepTwo)
                changeIconsBg(stepThree)
                changeIconsBg(stepFour)
                changeIconsBg(stepFive)
                changeIconsBg(stepSix)
                changeIconsBg(stepSeven)

                if (lastStep == null) {
                    val lastSteps = it.keyValueDao().getKeyValue(KeyValue.LAST_CURRENT_STEP)
                    lastStep = lastSteps.value
                }

                when(lastStep) {
                    DashboardStep.STEP_1 -> {
                        currentTask.type = stepOne.type
                        currentTask.isFirstCompleted = stepOne.isFirstCompleted
                        currentTask.isSecondCompleted = stepOne.isSecondCompleted
                    }
                    DashboardStep.STEP_2 -> {
                        currentTask.type = stepTwo.type
                        currentTask.isFirstCompleted = stepTwo.isFirstCompleted
                        currentTask.isSecondCompleted = stepTwo.isSecondCompleted
                    }
                    DashboardStep.STEP_3 -> {
                        currentTask.type = stepThree.type
                        currentTask.isFirstCompleted = stepThree.isFirstCompleted
                        currentTask.isSecondCompleted = stepThree.isSecondCompleted
                    }
                    DashboardStep.STEP_4 -> {
                        currentTask.type = stepFour.type
                        currentTask.isFirstCompleted = stepFour.isFirstCompleted
                        currentTask.isSecondCompleted = stepFour.isSecondCompleted
                    }
                    DashboardStep.STEP_5 -> {
                        currentTask.type = stepFive.type
                        currentTask.isFirstCompleted = stepFive.isFirstCompleted
                        currentTask.isSecondCompleted = stepFive.isSecondCompleted
                    }
                    DashboardStep.STEP_6 -> {
                        currentTask.type = stepSix.type
                        currentTask.isFirstCompleted = stepSix.isFirstCompleted
                        currentTask.isSecondCompleted = stepSix.isSecondCompleted
                    }
                    DashboardStep.STEP_7 -> {
                        currentTask.type = stepSeven.type
                        currentTask.isFirstCompleted = stepSeven.isFirstCompleted
                        currentTask.isSecondCompleted = stepSeven.isSecondCompleted
                    }
                }

                changeVisuals(currentTask)
            }
        }
    }

    private fun playClick(context : Context){
        if (mp.isPlaying) {
            mp.stop()
        }
        try {
            mp.reset()
            val afd: AssetFileDescriptor
            afd = context.assets.openFd("knobClick.mp3")
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mp.prepare()
            mp.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

private class BackgroundDrawable(
        val activity : Activity,
        val bottomLeftPoint: PointF,
        val topLeftSteeringWeelCorner : PointF
) : Drawable() {

    override fun draw(canvas: Canvas) {

        val screenSize = activity.getScreenSize()
        val bg = Paint()
        bg.style = Paint.Style.FILL
        bg.color = ContextCompat.getColor(activity, R.color.dashboard_bg_color)
        canvas.drawRect(0F, 0F, screenSize.width.toFloat(), screenSize.height.toFloat(), bg)

        val imageView = ImageView(activity)
        imageView.setImageResource(R.drawable.whale_bg)
        val drawable = imageView.drawable;
        drawable.setBounds(0, (screenSize.height * 0.66).toInt(), screenSize.width, screenSize.height);
        drawable.draw(canvas)

        val line = Paint()
        line.color = Color.WHITE
        line.style = Paint.Style.STROKE
        line.strokeWidth = activity.resources.getDimension(R.dimen.dashboard_stroke_width)//.dpToPx()

        // draw lines points
        val point_1 = PointF(topLeftSteeringWeelCorner.x,  topLeftSteeringWeelCorner.y)
        val point_2 = PointF(topLeftSteeringWeelCorner.x - 100,  topLeftSteeringWeelCorner.y - 100)
        val point_3 = PointF(point_2.x, bottomLeftPoint.y - activity.resources.getDimension(R.dimen.dashboard_stroke_width) / 2)
        val point_4 = PointF(point_3.x + 100,  point_3.y)
        // connecting lines
        canvas.drawLine(point_1.x, point_1.y, point_2.x, point_2.y, line)
        canvas.drawLine(point_2.x, point_2.y, point_3.x, point_3.y, line)
        canvas.drawLine(point_3.x, point_3.y, point_4.x, point_4.y, line)
    }

    @SuppressLint("WrongConstant")
    override fun getOpacity(): Int {
        return 0
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(cf: ColorFilter?) {}
}

class DashboardTask(dType : DashboardSteps) {

    private var _type : DashboardSteps? = null
    private var _isFirstCompleted : Boolean = false
    private var _isSecondCompleted : Boolean = true

    val typeObservable = PublishSubject.create<DashboardSteps>()
    val completedObservable = PublishSubject.create<Pair<Boolean, Boolean>>()

    var type : DashboardSteps?
        get() = _type
        set(value) {
            _type = value
            _type?.let { typeObservable.onNext(it) }
        }

    var isFirstCompleted : Boolean
        get() = _isFirstCompleted
        set(value) {
            if (_isFirstCompleted != value) {
                completedObservable.onNext(Pair(value, isSecondCompleted))
            }
            _isFirstCompleted = value
        }

    var isSecondCompleted : Boolean
        get() = _isSecondCompleted
        set(value) {
            if (_isSecondCompleted != value) {
                completedObservable.onNext(Pair(isFirstCompleted, value))
            }
            _isSecondCompleted = value

        }

    init {
        _type = dType
    }
}

enum class DashboardSteps {
    RESEARCH,
    WRITE_LETTER,
    SHARING,
    LETTER_CAMPAING,
    GOVERNMENT,
    PROTEST,
    HIJACK
}
