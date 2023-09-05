# 기능 설명
메모 작성 아이콘을 눌러 메모와 날짜를 선택하여 메모를 남길 수 있다. 남긴 메모는 리스트로 보여준다. 익명 로그인제로 여태 쓴 자신의 모든 메모를 볼 수 있다.

# 구조
구조를 그림으로 표현해봤다.
![](https://velog.velcdn.com/images/kuronuma_daisy/post/bd2c646b-132d-4928-b54e-ad537d898657/image.jpg)
* SplahActivity에서 로그인이 안 되어있으면 익명 회원가입하고 MainActivity로 넘어감.
* MainActivity에서 Dialog로 메모를 저장, 데이터 변화 발생 시 `.addValueEventListener`로 데이터를 받아옴.


# 완성 모습
* 첫 로그인 > 메모 남기기 > 재실행 시 기존 회원으로 로그인 > 기존 메모 리스트 + 새로운 메모
![](https://velog.velcdn.com/images/kuronuma_daisy/post/ade82d27-e5f2-4753-b290-e1f01b36be8e/image.gif)

# DialogView
다이얼로그에서는 DatePicker로 날짜를 선택하고, 작성한 메모를 함께 파이어베이스에 저장 한다.
```kotlin
// 메모 작성 다이얼로그 띄우기
        binding.writeBtn.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("운동 메모 다이얼로그")
            val mAlertDialog = mBuilder.show()

            var dateText = ""

            val DateSetBtn = mAlertDialog.findViewById<Button>(R.id.dateSelectBtn)

            DateSetBtn?.setOnClickListener {

                val today = GregorianCalendar()
                val year: Int = today.get(Calendar.YEAR)
                val month: Int = today.get(Calendar.MONTH)
                val date: Int = today.get(Calendar.DATE)

                val dlg = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int
                    ) {
                        //month는 +1 해야 함
                        Log.d("MAIN", "${year}, ${month+1}, ${dayOfMonth}")

                        DateSetBtn.setText("${year}/${month+1}/${dayOfMonth}")
                        dateText = "${year}/${month+1}/${dayOfMonth}"
                    }

                }, year, month, date)
                dlg.show()
            }
            
            // 다이얼로그의 저장 버튼 클릭시
            val saveBtn = mAlertDialog.findViewById<Button>(R.id.saveBtn)
            saveBtn?.setOnClickListener {
                val dietMemo = mAlertDialog.findViewById<EditText>(R.id.dietMemo)?.text.toString()

                val model = DataModel(dateText, dietMemo)

                val database = Firebase.database
                val myRef = database.getReference("myMemo").child(Firebase.auth.currentUser!!.uid)

                myRef.push().setValue(model)

                // 저장했으면 다이얼로그 닫기
                mAlertDialog.dismiss()
            }
        }
```

# Firebase 파이어베이스
https://firebase.google.com/docs/database/android/start?hl=ko
먼저 위 공식문서를 보고 필요한 SDK를 그래들에 추가!

## 파이어베이스 데이터 불러오기
파이어베이스에서 "mymemo" 트리에 있는 데이터를 "date"순으로 불러옴. `orderByChild("date")`
가져온 데이터는 ListView를 통해 보여줌.
```kotlin
    val dataModelList = mutableListOf<DataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val database = Firebase.database
        val myRef = database.getReference("myMemo")

        val listView = binding.mainLV
        val myadapter = ListViewAdapter(dataModelList)
        listView.adapter = myadapter

        // listView에 넣을 메모 목록 Firebase에서 가져오기
        // .orderByChild("date") <<를 추가하여 날짜순 정렬
        myRef.child(Firebase.auth.currentUser!!.uid).orderByChild("date").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                dataModelList.clear()

                //snapshot에는 전체가 들어있음
                for(dataModel in snapshot.children){
                    Log.d("Data", dataModel.toString())

                    dataModelList.add(dataModel.getValue(DataModel::class.java)!!)
                }
                myadapter.notifyDataSetChanged()

                Log.d("DataList", dataModelList.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
}
```
## 파이어베이스 데이터 저장하기
위에 소개한 Dialog 목차의 코드와 겹친다.
* dateText와 dietMemo를 DataModel로 묶어 저장한다.
* `myRef.push().setValue(model)`
* `push()`가 없으면 같은 데이터가 있으면 다시 저장하지 않는다. push()가 있으면 같은 데이터가 이미 있어도 추가로 저장한다.
```kotlin
            // 다이얼로그의 저장 버튼 클릭시
            val saveBtn = mAlertDialog.findViewById<Button>(R.id.saveBtn)
            saveBtn?.setOnClickListener {
                val dietMemo = mAlertDialog.findViewById<EditText>(R.id.dietMemo)?.text.toString()

                val model = DataModel(dateText, dietMemo)

                val database = Firebase.database
                val myRef = database.getReference("myMemo").child(Firebase.auth.currentUser!!.uid)

                myRef.push().setValue(model)

                // 저장했으면 다이얼로그 닫기
                mAlertDialog.dismiss()
            }
```
# [벨로그에서 보기](https://velog.io/@kuronuma_daisy/Android-%EC%9A%B4%EB%8F%99-%EB%A9%94%EB%AA%A8-%EC%95%B1-Dialog-ListView-%ED%99%9C%EC%9A%A9-Firebase-Authentication-Realtime-Database-%ED%99%9C%EC%9A%A9)
# [깃허브에서 전체 코드 보기](https://github.com/yndoo/DietMemoApp/tree/master)


조금 더 예쁘게 꾸며 친구들이 사용할 수 있게 만들어 보고 싶다.
