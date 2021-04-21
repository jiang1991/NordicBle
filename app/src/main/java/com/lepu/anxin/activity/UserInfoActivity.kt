package com.lepu.anxin.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.blankj.utilcode.util.LogUtils
import com.lepu.anxin.R
import com.lepu.anxin.room.Addr
import com.lepu.anxin.viewmodel.AppViewModel
import com.lepu.anxin.viewmodel.UserInfoViewModel
import kotlinx.android.synthetic.main.activity_user_info.*
import java.util.*


class UserInfoActivity : AppCompatActivity() {

    private val userViewModel: UserInfoViewModel by viewModels()
    lateinit var appViewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        appViewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(application)).get(AppViewModel::class.java)
        addLiveDataObserve()

        initUi()
    }


    private fun initUi() {


        name_edit.setOnClickListener {
            hideImm()
            name_sw.showNext()
            if (name_et.text.toString().isNotEmpty()) {
                userViewModel.name.value = name_et.text.toString()
            }
        }

        phone_edit.setOnClickListener {
            hideImm()
            phone_sw.showNext()
            if (phone_et.text.toString().isNotEmpty()) {
                userViewModel.phone.value = phone_et.text.toString()
            }
        }
        birth_edit.setOnClickListener {
            hideImm()
//            val start = Calendar.getInstance()
//            start.set(1900, 0, 0)
//            val end = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"))
//            end.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)
            val current = Calendar.getInstance()
            current.set(1970, 0, 1)
            val birthPicker = TimePickerBuilder(this) { date, v ->
                LogUtils.d("$date")
                val c = Calendar.getInstance()
                c.time = date
                userViewModel.birth.value = "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)+1}-${c.get(Calendar.DAY_OF_MONTH)}"
            }
                    .setDate(current)
                    .setType(booleanArrayOf(true, true, true, false, false, false))
                    .build()
            birthPicker.show()

        }
        gender_edit.setOnClickListener {
            hideImm()
            val genderPicker = OptionsPickerBuilder(this) { options1, options2, options3, v ->
//                LogUtils.d("$options1, $options2, $options3")
                when(options1) {
                    0 -> userViewModel.gender.value = "男"
                    1 -> userViewModel.gender.value = "女"
                }

            }
                    .build<String>()
            val list = mutableListOf<String>()
            list.add("男")
            list.add("女")
            genderPicker.setPicker(list)
            genderPicker.show()
        }
        height_edit.setOnClickListener {
            hideImm()
            val heightPicker = OptionsPickerBuilder(this) { options1, options2, options3, v ->
                userViewModel.height.value = 50+options1
            }
                    .build<Int>()
            val list = mutableListOf<Int>()
            for (i in 50 .. 220) {
                list.add(i)
            }
            heightPicker.setPicker(list)
            heightPicker.setSelectOptions(120)
            heightPicker.show()
        }
        weight_edit.setOnClickListener {
            hideImm()
            val weightPicker = OptionsPickerBuilder(this) { options1, options2, options3, v ->
                userViewModel.weight.value = 30+options1
            }
                    .build<Int>()

            val list = mutableListOf<Int>()
            for (i in 30 .. 150) {
                list.add(i)
            }
            weightPicker.setPicker(list)
            weightPicker.setSelectOptions(80)
            weightPicker.show()
        }
        id_edit.setOnClickListener {
            hideImm()
            id_sw.showNext()
            if (id_et.text.toString().isNotEmpty()) {
                userViewModel.nationId.value = id_et.text.toString()
            }
        }
        city_edit.setOnClickListener {
            hideImm()
            Addr.initAddrs(this)
            val cityPicker = OptionsPickerBuilder(this) { options1, options2, options3, v ->
                userViewModel.city.value = "${Addr.proviences[options1]} ${Addr.citys[options1][options2]} ${Addr.diss[options1][options2][options3]}"
            }
                    .build<String>()
            cityPicker.setPicker(Addr.proviences, Addr.citys, Addr.diss)
            cityPicker.show()

        }
        road_edit.setOnClickListener {
            hideImm()
            road_sw.showNext()
            if (road_et.text.toString().isNotEmpty()) {
                userViewModel.road.value = road_et.text.toString()
            }
        }

        save.setOnClickListener {
            save()
        }
    }

    private fun addLiveDataObserve() {
        appViewModel.userInfo.observe(this, {
            userViewModel.name.postValue(it.name)
            userViewModel.phone.postValue(it.phone)
            userViewModel.gender.postValue(it.gender)
            userViewModel.birth.postValue(it.birth)
            userViewModel.height.postValue(it.height)
            userViewModel.weight.postValue(it.weight)
            userViewModel.nationId.postValue(it.nationId)
            userViewModel.city.postValue(it.city)
            userViewModel.road.postValue(it.road)
        })

        userViewModel.name.observe(this, {
            name_tv.text = it
        })
        userViewModel.phone.observe(this, {
            phone_tv.text = it
        })
        userViewModel.gender.observe(this, {
            gender_tv.text = it
        })
        userViewModel.birth.observe(this, {
            birth_tv.text = it
        })
        userViewModel.height.observe(this, {
            if (it != 0) {
                height_tv.text = "$it cm"
            }
        })
        userViewModel.weight.observe(this, {
            if (it != 0) {
                weight_tv.text = "$it kg"
            }
        })
        userViewModel.nationId.observe(this, {
            id_tv.text = it
        })
        userViewModel.city.observe(this, {
            city_tv.text = it
        })
        userViewModel.road.observe(this, {
            road_tv.text = it
        })
    }

    private fun save() {
        if (userViewModel.name.value == null
            || userViewModel.phone.value == null
            || userViewModel.gender.value == null
            || userViewModel.birth.value == null
        )
            {
            Toast.makeText(this, "请补全必要用户信息", Toast.LENGTH_SHORT).show()
            return
        }

        val weight: Int = if (userViewModel.weight.value == null) {
            0
        } else {
            userViewModel.weight.value!!
        }

        val height: Int = if (userViewModel.height.value == null) {
            0
        } else {
            userViewModel.height.value!!
        }


        appViewModel.saveUserInfo(
            userViewModel.name.value!!,
            userViewModel.phone.value!!,
            userViewModel.gender.value!!,
            userViewModel.birth.value!!,
            height,
            weight,
            userViewModel.nationId.value,
            userViewModel.city.value,
            userViewModel.road.value
        )

        toNext()
    }

    
    private fun hideImm() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(this.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun toNext() {
        val i = Intent(this, ServerConfigActivity::class.java)
        startActivity(i)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        //
    }
}