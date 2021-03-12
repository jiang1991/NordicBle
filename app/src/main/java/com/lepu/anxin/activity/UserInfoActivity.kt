package com.lepu.anxin.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.blankj.utilcode.util.LogUtils
import com.lepu.anxin.R
import com.lepu.anxin.room.Addr
import com.lepu.anxin.room.UserInfo
import com.lepu.anxin.viewmodel.UserInfoViewModel
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_user_info.*
import org.bson.types.ObjectId
import java.util.*


class UserInfoActivity : AppCompatActivity() {

    private val userViewModel: UserInfoViewModel by viewModels()
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        initRealm()

        addLiveDataObserve()

        initUi()
    }

    private fun initRealm() {
        val config = RealmConfiguration.Builder()
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .inMemory()
            .build()

        realm = Realm.getInstance(config)
    }

    private fun initUi() {

        /**
         * read last user info
         */
        val info = realm.where(UserInfo::class.java).findFirst()
        LogUtils.d(info.toString())
        info?.apply {
            userViewModel.name.value = this.name
            userViewModel.phone.value = this.phone
            userViewModel.gender.value = this.gender
            userViewModel.birth.value = this.birth
            userViewModel.height.value = this.height
            userViewModel.weight.value = this.weight
            userViewModel.nationId.value = this.nationId
            userViewModel.city.value = this.city
            userViewModel.road.value = this.road
        }


        name_edit.setOnClickListener {
            name_sw.showNext()
            if (name_et.text.toString().isNotEmpty()) {
                userViewModel.name.value = name_et.text.toString()
            }
        }

        phone_edit.setOnClickListener {
            phone_sw.showNext()
            if (phone_et.text.toString().isNotEmpty()) {
                userViewModel.phone.value = phone_et.text.toString()
            }
        }
        birth_edit.setOnClickListener {
//            val start = Calendar.getInstance()
//            start.set(1900, 0, 0)
//            val end = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"))
//            end.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)
            val current = Calendar.getInstance()
            current.set(1970, 0, 1)
            val birthPicker = TimePickerBuilder(this) { date, v ->
                LogUtils.d("$date")
                userViewModel.birth.value = "${date.year}/${date.month+1}/${date.day}"
            }
                    .setDate(current)
                    .setType(booleanArrayOf(true, true, true, false, false, false))
                    .build()
            birthPicker.show()
        }
        gender_edit.setOnClickListener {
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
            val heightPicker = OptionsPickerBuilder(this) { options1, options2, options3, v ->
                userViewModel.height.value = 50+options1
            }
                    .build<Int>()
            val list = mutableListOf<Int>()
            for (i in 50 .. 220) {
                list.add(i)
            }
            heightPicker.setPicker(list)
            heightPicker.show()
        }
        weight_edit.setOnClickListener {
            val weightPicker = OptionsPickerBuilder(this) { options1, options2, options3, v ->
                userViewModel.weight.value = 50+options1
            }
                    .build<Int>()

            val list = mutableListOf<Int>()
            for (i in 30 .. 100) {
                list.add(i)
            }
            weightPicker.setPicker(list)
            weightPicker.show()
        }
        id_edit.setOnClickListener {
            id_sw.showNext()
            if (id_et.text.toString().isNotEmpty()) {
                userViewModel.nationId.value = id_et.text.toString()
            }
        }
        city_edit.setOnClickListener {
            Addr.initAddrs(this)
            val cityPicker = OptionsPickerBuilder(this) { options1, options2, options3, v ->
                userViewModel.city.value = "${Addr.proviences[options1]} ${Addr.citys[options1][options2]} ${Addr.diss[options1][options2][options3]}"
            }
                    .build<String>()
            cityPicker.setPicker(Addr.proviences, Addr.citys, Addr.diss)
            cityPicker.show()

        }
        road_edit.setOnClickListener {
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
            height_tv.text = "$it cm"
        })
        userViewModel.weight.observe(this, {
            weight_tv.text = "$it kg"
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
        if (userViewModel.name.value == null || userViewModel.phone.value == null || userViewModel.gender.value == null ||  userViewModel.birth.value == null) {
            Toast.makeText(this, "请补全必要用户信息", Toast.LENGTH_SHORT).show()
            return
        }

//        val info = UserInfo(
//            0,
//            userViewModel.name.value!!,
//            userViewModel.phone.value!!,
//            userViewModel.gender.value!!,
//            userViewModel.birth.value!!,
//            userViewModel.height.value,
//            userViewModel.weight.value,
//            userViewModel.nationId.value,
//            userViewModel.city.value,
//            userViewModel.road.value
//        )


        realm.executeTransaction {
            val info = it.createObject(UserInfo::class.java, ObjectId())
            info.name = userViewModel.name.value!!
            info.phone = userViewModel.phone.value!!
            info.gender = userViewModel.gender.value!!
            info.birth = userViewModel.birth.value!!
            info.height = userViewModel.height.value
            info.weight = userViewModel.weight.value
            info.nationId = userViewModel.nationId.value
            info.city = userViewModel.city.value
            info.road = userViewModel.road.value

//            it.insertOrUpdate(info)
            LogUtils.d("保存成功 ${info.toString()}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onBackPressed() {
        //
    }
}