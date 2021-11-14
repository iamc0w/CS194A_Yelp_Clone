package com.example.yelpclone

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "FO7mfcxxC0kG6BYhNcbSUcIFPMduMEoZwApkZp-XMkWibkOErOTZCAZ0SXPsOOTQ9pCNNIEZH-VqKMYrPUBINamVtB2pNxhwm0JIhT84tdC3i86kGl9gzswKG5KHYXYx"
private const val DEFAULT_LOCATION = "New York"
private const val DEFAULT_QUERY = "Avocado Toast"

class MainActivity : AppCompatActivity() {
    private lateinit var restaurants: MutableList<YelpRestaurant>
    private lateinit var adapter: RestaurantsAdapter
    private lateinit var yelpService: YelpService
    private lateinit var etSearch : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        setContentView(R.layout.activity_main)

        restaurants = mutableListOf<YelpRestaurant>()
        adapter = RestaurantsAdapter(this, restaurants)
        val rvRestaurants : RecyclerView = findViewById(R.id.rvRestaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        yelpService = retrofit.create(YelpService::class.java)
        make_yelp_request(DEFAULT_QUERY, DEFAULT_LOCATION)

        etSearch = findViewById(R.id.etSearch)
        val ibSearch : ImageButton = findViewById(R.id.ibSearch)

        ibSearch.setOnClickListener {
            Log.i(TAG, "clicked on search button")
            new_search()
        }

        etSearch.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                Log.i(TAG, "enter key")
                new_search()
                return@OnKeyListener true
            }
            false
        })
    }

    private fun new_search() {
        var query : String = etSearch.text.toString()
        if (query.isEmpty()) query = DEFAULT_QUERY
        make_yelp_request(query, DEFAULT_LOCATION)
    }

    private fun make_yelp_request(searchTerm: String, location: String) {
        restaurants.clear()
        yelpService.searchRestaurants("Bearer $API_KEY",searchTerm, location).enqueue(object : Callback<YelpSearchResult> {
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onResponse$response")
                val body = response.body()
                if (body == null) {
                    Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                    return
                }
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG, "onFailure")
            }
        })
    }

}