package com.example.yelpclone

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.SearchView
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

class MainActivity : AppCompatActivity() {
    private lateinit var restaurants: MutableList<YelpRestaurant>
    private lateinit var adapter: RestaurantsAdapter
    private lateinit var yelpService: YelpService

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

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun make_yelp_request(searchTerm: String, location: String) {
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


    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Log.i(TAG, "handleIntent with query $query")
            if (query != null) {
                make_yelp_request(query, DEFAULT_LOCATION)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        return true
    }

}