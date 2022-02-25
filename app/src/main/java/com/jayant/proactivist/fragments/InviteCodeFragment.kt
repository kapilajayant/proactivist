package com.jayant.proactivist.fragments

import android.content.DialogInterface
import com.jayant.proactivist.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.jayant.proactivist.models.ListResponseModel
import com.jayant.proactivist.models.ResponseModel
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import com.jayant.proactivist.utils.DialogHelper
import com.jayant.proactivist.utils.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InviteCodeFragment(val inviteCodeCallback: InviteCodeCallback) : BottomSheetDialogFragment() {

    lateinit var apiService: APIService
    private var currentUser: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        apiService = ApiUtils.getAPIService()
        currentUser = FirebaseAuth.getInstance().currentUser

        val view = inflater.inflate(R.layout.layout_add_invite_code, container, false)
        val button = view.findViewById<Button>(R.id.btn_done)
        val et_invite_code = view.findViewById<EditText>(R.id.et_invite_code)

        val prefManager = PrefManager(requireContext())
        et_invite_code.setText(prefManager.inviteCode)

        button.setOnClickListener {
        val inviteCode = et_invite_code.text.toString()
            if(inviteCode.isNullOrEmpty().not()){
                addInviteCode(inviteCode)
            }
            else{
                Toast.makeText(requireContext(), "Invite code missing", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun addInviteCode(inviteCode: String) {
        DialogHelper.showLoadingDialog(requireActivity())
        currentUser?.uid?.let { uid ->
            apiService.addInviteCode(inviteCode, uid).enqueue(object : Callback<ResponseModel> {
                override fun onResponse(
                    call: Call<ResponseModel>,
                    response: Response<ResponseModel>
                ) {
                    DialogHelper.hideLoadingDialog()
                    if (response.isSuccessful) {
                        try {
                            if (response.code() == 200) {
                                inviteCodeCallback.addedInviteCode()
                                Toast.makeText(requireContext(), "Code applied successfully!", Toast.LENGTH_SHORT).show()
                                dismiss()
                            }
                            else {
                                Toast.makeText(
                                    requireContext(),
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                            }
                        } catch (e: Exception) {
                            DialogHelper.hideLoadingDialog()
                            e.printStackTrace()
                        }
                    }

                    else{
                        try {
                            val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ResponseModel::class.java)
                            Toast.makeText(requireContext(), errorResponse.message, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    DialogHelper.hideLoadingDialog()
                    t.printStackTrace()
                }
            })
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val prefManager = PrefManager(requireContext())
        prefManager.showInviteCode = false
    }

    interface InviteCodeCallback{
        fun addedInviteCode()
    }
}