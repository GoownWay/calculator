package com.example.administrator.calculator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Stack;

public class Calculate extends AppCompatActivity {

    TextView textView;
    String expression = "";//表达式
    String result = "";//计算结果
    boolean end = false;//表达式是否结束的标志，true为表达式结束
    boolean hasPoint = false;//是否可以继续加小数点，false为可以加，true为不可以
    boolean hasPercent = false;//是否可以继续加百分号，false为可以加
    int id[] = {R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5, R.id.key6,
            R.id.key7, R.id.key8, R.id.key9, R.id.keypoint, R.id.keyPlus, R.id.keyPercent,
            R.id.keyHistory, R.id.keyC, R.id.keyBackspace, R.id.keyMultiplication, R.id.keyMinus,
            R.id.keyDivision, R.id.keyEquality};//按钮的id

    //菜单的可见
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calculatormenu,menu);
        return true;
    }

    //点击菜单选项的响应
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout://跳转到到登录界面
                Intent intent = new Intent(Calculate.this,Login.class);
                startActivity(intent);
                finish();
                break;
            case R.id.exit://退出程序
                ActivityCollector.finshAll();//////////////////////////////
                android.os.Process.killProcess(android.os.Process.myPid());//////////////////////
                break;
            case R.id.clearAll://清空历史计算记录
                String s = load().trim();
                if(s.length()!=0){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Calculate.this);
                    dialog.setTitle("This is Dialog");
                    dialog.setMessage("你确定要删除历史计算记录吗？");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearAll();
                            Toast.makeText(Calculate.this,"已清空记录！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.setNegativeButton("取消",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                }else Toast.makeText(Calculate.this,"历史记录为空！", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

    //计算器按钮监听和响应
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);
        ActivityCollector.addActivity(this);//////////////////////////
        textView = (TextView) findViewById(R.id.calculateShow);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        for (int i = 0; i < id.length; i++) {//通过循环，将所有的按钮添加监听
            Button numberButton = (Button) findViewById(id[i]);
            numberButton.setOnClickListener(new View.OnClickListener() {
                //按钮的响应
                @Override
                public void onClick(View v) {
                    Button b = (Button) v.findViewById(v.getId());
                    String current = b.getText().toString();
                    if (current.matches("^[0-9]+$")) {//0-9的响应
                        if (end) {//按了一下等于后再按数字则显示数字，而上一次的结果清空
                            expression = "";
                            end = false;
                        }
                        if (expression.length() >= 1) {//表达式的长度是否大于等于1
                            if(expression.charAt(expression.length()-1)=='%')
                                //看上个按的是否是百分号，如果是怎么不会继续在表达式上添加百分号
                                current = "";
                            char tmp1 = expression.charAt(expression.length() - 1);
                            //如果表达式只有一个数字，而且第一个按的是0，则再次按其他数字的时候，把0变为按的数字
                            if (tmp1 == '0' && expression.length() == 1) {
                                expression = expression.substring(0, expression.length() - 1);
                            } else if (tmp1 == '0' && expression.length() > 1) {
                                //如果前一个按的是0且长度大于1，则看再上一个按的是否是加减乘除，如果是的话也将0变为当前按的数
                                char tmp2 = expression.charAt(expression.length() - 2);
                                if (tmp2 == '+' || tmp2 == '-' || tmp2 == '×' || tmp2 == '÷') {
                                    expression = expression.substring(0, expression.length() - 1);
                                }
                            }
                        }
                        expression += current;
                        textView.setText(expression);
                    }

                    switch (v.getId()) {
                        case R.id.keyHistory: {//查看历史记录
                            String string = load().trim();
                            if(string.length()!=0){
                                Intent intent = new Intent(Calculate.this,history.class);
                                intent.putExtra("history",string);//按查询到的历史记录字符串传到另一个界面
                                startActivity(intent);
                            }else Toast.makeText(Calculate.this,"历史计算记录为空！", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case R.id.keyC: {//清除键
                            end = false;
                            expression = "";
                            hasPoint = false;
                            hasPercent = false;
                            textView.setTextSize(60);
                            textView.setText(expression);
                            break;
                        }
                        case R.id.keyBackspace: {//退格键
                            if (end) {//如果上一个运算结束后按退格键，则吧结果显示出来，并且执行退格操作
                                expression = result;
                                end = false;
                            }
                            if(expression.length()==14)//如果表达式的长度大于14则把文字变小
                                textView.setTextSize(60);
                            if (expression.length() > 1) {
                                //如果表达式长度大于1并且上一个是点，则执行退格操作后把是否可以继续按点的标志置为可以继续按点
                                if(expression.charAt(expression.length()-1)=='.')
                                    hasPoint = false;
                                //如果表达式长度大于1并且上一个是百分号，则执行退格操作后把是否可以继续按百分号的标志置为可以继续按百分号
                                if(expression.charAt(expression.length()-1)=='%')
                                    hasPercent = false;
                                //执行表达式退格
                                expression = expression.substring(0, expression.length() - 1);
                                int i = expression.length() - 1;
                                for (; i >= 0; i--) {
                                    char tmpFront = expression.charAt(i);
                                    if (tmpFront == '.' || tmpFront == '+' || tmpFront == '-' || tmpFront == '×') {
                                        break;
                                    }
                                }
                            } else if (expression.length() == 1) {
                                //表达式长度为1则把表达式置为没有
                                expression = "";
                            }
                            textView.setText(expression);
                            break;
                        }
                        case R.id.keyPercent: {//百分号
                            if (end) {//如果上一个运算结束后按百分号，则吧结果显示出来，并且加上百分号
                                expression = result;
                                end = false;
                            }
                            if (!hasPercent) {//如果可以继续按百分号
                            if (expression.length() > 0) {
                                //如果表达式的长度大于0并且前一个按的字符不为加减乘除、点、百分号则可以继续按百分号
                                char a = expression.charAt(expression.length() - 1);
                                if (!(a == '%' || a == '.' || a == '+' || a == '-' || a == '×' || a == '÷'))
                                {
                                    expression += current;
                                    hasPercent = true;
                                }
                            } else {
                                break;
                            }
                        }
                            textView.setText(expression);
                            break;
                        }
                        case R.id.keyDivision: {//除号
                            if (end) {//如果上一个运算结束后按除号，则吧结果显示出来，并且加上除号
                                expression = result;
                                end = false;
                            }
                            if(expression.length()>0)
                            {//如果表达式的长度大于0并且前一个按的字符不为加减乘除、点则可以继续按除号
                                char a = expression.charAt(expression.length()-1);
                                if(!(a == '.' || a == '+' || a == '-' || a == '×'|| a == '÷'))
                                    expression += current;
                                hasPoint = false;
                                hasPercent = false;
                            }else {
                                break;
                            }
                            textView.setText(expression);
                            break;
                        }
                        case R.id.keyMultiplication: {//乘号
                            if (end) {//如果上一个运算结束后按乘号，则吧结果显示出来，并且加上乘号
                                expression = result;
                                end = false;
                            }
                            if(expression.length()>0)
                            {//如果表达式的长度大于0并且前一个按的字符不为加减乘除、点则可以继续按乘号
                                char a = expression.charAt(expression.length()-1);
                                if(!(a == '.' || a == '+' || a == '-' || a == '×'|| a == '÷'))
                                    expression += current;
                                hasPoint = false;
                                hasPercent = false;
                            }else {
                                break;
                            }
                            textView.setText(expression);
                            break;
                        }
                        case R.id.keyMinus: {//减号
                            if (end) {//如果上一个运算结束后按减号，则吧结果显示出来，并且加上减号
                                expression = result;
                                end = false;
                            }
                            if(expression.length()>0)
                            {
                                char a = expression.charAt(expression.length()-1);
                                if(a == '+')
                                    //如果上一个按的是加号然后这一次按了减号则把上一次的加号去掉然后加上减号
                                    expression = expression.substring(0,expression.length()-1);
                                if(!(a == '.' || a == '-' ))//上一次按了减号和点之后不能继续按减号
                                    expression += current;
                                hasPoint = false;
                                hasPercent = false;
                            }else if(expression.length() == 0){//如果表达式长度为0则把这个置为负数
                                expression +=current;
                                hasPoint = false;
                                hasPercent = false;
                            }
                            textView.setText(expression);
                            break;
                        }
                        case R.id.keyPlus: {//加号
                            if (end) {//如果上一个运算结束后按加号，则吧结果显示出来，并且加上加号
                                expression = result;
                                end = false;
                            }
                            if(expression.length()>0)
                            {//如果表达式的长度大于0并且前一个按的字符不为加减乘除、点则可以继续按加号
                                char a = expression.charAt(expression.length()-1);
                                if(!(a == '.' || a == '+' || a == '-' || a == '×'|| a == '÷'))
                                    expression += current;
                                hasPoint = false;
                                hasPercent = false;
                            }else {
                                break;
                            }
                            textView.setText(expression);
                            break;
                        }
                        case R.id.keyEquality: {//等号
                            if (end) {//如果按了一次等号后再次按等号，显示的是上一次运算的最终结果
                                expression = result;
                                textView.setText(expression);
                                break;
                            }
                            if(!expression.equals("")){//表达式不能为空
                                String tmp = expression.substring(expression.length()-1,expression.length());
                                if(!tmp.equals("+") && !tmp.equals("-") && !tmp.equals("×") && !tmp.equals("÷") ){
                                    //如果上一个按的不是加减乘除，则可以按等号，否则提示表达式错误
                                    ArrayList<String> arrayList = strFormat(expression);//将表达式按顺序分割包含成数字和运算符的集合
                                        result = ""+calculate(arrayList);//结果计算
                                    if(result.equals("null")) {
                                        Toast.makeText(Calculate.this,"除数不能为0！", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    if(result.indexOf(".") >0 ){
                                        //按了加减乘除等于操作符后判断结果是否有小数点如果没有，则把标志置为可以按
                                        result = result.replaceAll("0+?$","");
                                        result = result.replaceAll("[.]$","");
                                    }
                                    expression = expression +"="+ result;
                                    //记录存储
                                    save(expression);
                                    //一个算式的结束
                                    end = true;
                                    textView.setText(expression);
                                    //重置百分号和点是否被点击的状态
                                    hasPoint = false;
                                    hasPercent = false;
                                }else Toast.makeText(Calculate.this,"输入的有误！", Toast.LENGTH_SHORT).show();
                            }else Toast.makeText(Calculate.this,"输入的内容为空！", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case R.id.keypoint: {//点
                            if (end) {//如果结果包含小数点，继续按点显示上一个表达式的结果，而不会继续在结果上加点
                                if(result.matches("\\d+\\.\\d+$"))
                                    hasPoint = true;
                                expression = result;
                                end = false;
                            }
                            if(!hasPoint){
                                if(expression.equals(""))
                                {//表达式为空的话，按点显示0.
                                    expression = "0" + current;
                                    hasPoint = true;
                                }else {
                                    char a = expression.charAt(expression.length()-1);
                                    if(!(a == '%'||a == '.' || a == '+' || a == '-' || a == '×'|| a == '÷'))
                                    {//上一个按的不是加减乘除、点、百分号才在表达式上添加一个点
                                        expression += current;
                                        hasPoint = true;
                                    }
                                }
                            }
                            textView.setText(expression);
                            break;
                        }
                    }
                    if (expression.length() > 13)//当长度大于13的时候，字体变小
                        textView.setTextSize(40);
                }
            });
        }
    }

    //结果储存
    public void save(String exp){
        FileOutputStream out =null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("data", Context.MODE_APPEND);//累加的方式存储
            writer = new BufferedWriter(new OutputStreamWriter(out));
            exp = exp+"\n";//每一条结果后面加上换行符
            writer.write(exp);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer!=null){
                    writer.close();
                }
            } catch (Exception e1){
                e1.printStackTrace();
            }
        }
    }

    //结果读取
    public String load(){
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = openFileInput("data");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null){
                if(content.length()==0)
                    content.append(line);
                else
                    content.append(" "+line);//把每一行的历史记录用空格分隔开，方便把历史记录变为一个个字符串
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    //结果清除
    public void clearAll(){
        FileOutputStream out =null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("data", Context.MODE_PRIVATE);//以覆盖的方式存储，把历史记录清空
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write("");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer!=null){
                    writer.close();
                }
            } catch (Exception e1){
                e1.printStackTrace();
            }
        }
    }

    //计算
    public static Object calculate(ArrayList<String> obj){
        ArrayList<String> result = infixToSuffix(obj);
        Stack<Double> stack = new Stack<>();
        for(int i = 0;i < result.size();i++){
            String symbol = result.get(i);
            if(isDigital(symbol)){//如果是数字则把数字压入栈中
                stack.push(Double.parseDouble(symbol));
            }else{
                //如果遇到操作符，则把栈顶的两个元素取出，然后进行相应的加减乘除操作，最后把结果再压入栈中，最后栈只剩下一个元素就是结果
                Double num1,num2;
                num1 = stack.pop();
                num2 = stack.pop();
                switch (symbol){
                    case "+":
                        stack.push(num2 + num1);
                        break;
                    case "-":
                        stack.push(num2 - num1);
                        break;
                    case "×":
                        stack.push(num2 * num1);
                        break;
                    case "÷":
                    {
                        if(num1>-0.000001 && num1<0.000001)//如果除数即分母为0，则返回空
                            return null;
                        stack.push(num2/num1);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return String.format("%.8f",stack.pop());//有很长的小数的把结果保留八位小数
    }

    //将表达式字符串分割
    public static  ArrayList<String> strFormat(String src){
        if(src == null || src.equals(""))
            return null;
        ArrayList<String> arrayList = new ArrayList<>();
        StringBuilder comChar = new StringBuilder();
        for(int i = 0;i < src.length();i++){
            char ch  = src.charAt(i);
            //判断上一个是否是*或者/如果是的话，下一个/或者*后面的-为负号
            char lastchar = i>0?src.charAt(i-1):src.charAt(i);
            if(ch ==' '){
                continue;
            }
            //点，0-9，%，或者负号都跳过
            if(!String.valueOf(ch).matches("^[0-9]+$")&&!String.valueOf(ch).matches("\\.")
                    &&!String.valueOf(lastchar).equals("×")&&!String.valueOf(lastchar).equals("÷")
                    &&!String.valueOf(lastchar).equals("-")&&!String.valueOf(ch).equals("%")){//Character.isDigit(ch)||
                if(!comChar.toString().trim().equals("")){
                    //如果前面有%号那么将其乘0.01后再添加
                    if(comChar.toString().trim().charAt(comChar.length()-1)=='%')
                        arrayList.add(String.valueOf(Double.parseDouble(comChar.toString().trim().substring(0, comChar.length()-1))*0.01));
                    else
                        arrayList.add(comChar.toString().trim());
                    comChar.delete(0,comChar.length());
                }
                arrayList.add(ch+"");
                continue;
            }
            comChar.append(ch);
        }
        if(!comChar.toString().trim().equals("")){
            //如果前面有%号那么将其乘0.01后再添加
            if(comChar.toString().trim().charAt(comChar.length()-1)=='%')
                arrayList.add(String.valueOf(Double.parseDouble(comChar.toString().trim().substring(0, comChar.length()-1))*0.01));
            else
                arrayList.add(comChar.toString().trim());
        }
        return arrayList;
    }

    //判断是否是数字
    public static boolean isDigital(String symbol){//只要不是加减乘除和括号就判断为是数字
        return !symbol.equals("+")&&!symbol.equals("-")&&!symbol.equals("×")&&!symbol.equals("÷")
                &&!symbol.equals("(")&&!symbol.equals(")");
    }

    //中缀表达式转为后缀表达式
    public static ArrayList<String> infixToSuffix(ArrayList<String> exp) {
        // 创建操作符堆栈
        Stack<String> s = new Stack<String>();
        ArrayList<String> st = new ArrayList<>();
        int length = exp.size(); // 输入的中缀表达式的长度
        for (int i = 0; i < length; i++) {
            String temp;// 临时字符变量
            // 获取该中缀表达式的每一个字符并进行判断
            String ch = exp.get(i);
            switch (ch) {
                // 忽略空格
                case "":
                    break;
                // 如果是左括号直接压入堆栈
                case "(":
                    s.push(ch);
                    break;

                // 碰到'+' '-'，将栈中的所有运算符全部弹出去，直至碰到左括号为止，输出到队列中去
                case "+":
                case "-":
                    while (s.size() != 0) {
                        temp = s.pop();
                        if (temp == "(") {
                            // 重新将左括号放回堆栈，终止循环
                            s.push("(");
                            break;
                        }
                        st.add(temp);
                    }
                    // 没有进入循环说明是当前为第一次进入或者其他前面运算都有括号等情况导致栈已经为空,此时需要将符号进栈
                    s.push(ch);
                    break;
                // 如果是乘号或者除号，则弹出所有序列，直到碰到加好、减号、左括号为止，最后将该操作符压入堆栈
                case "×":
                case "÷":
                    while (s.size() != 0) {
                        temp = s.pop();
                        // 只有比当前优先级高的或者相等的才会弹出到输出队列，遇到加减左括号，直接停止当前循环
                        if (temp.equals("+") || temp.equals("-") || temp.equals("(")) {
                            s.push(temp);
                            break;
                        } else {
                            st.add(temp);
                        }
                    }
                    // 没有进入循环说明是当前为第一次进入或者其他前面运算都有括号等情况导致栈已经为空,此时需要将符号进栈
                    s.push(ch);
                    break;
                // 如果碰到的是右括号，则距离栈顶的第一个左括号上面的所有运算符弹出栈并抛弃左括号
                case ")":
                    // 这里假设一定会遇到左括号了，此为自己改进版，已经验证可以过
                    // while ((temp = s.pop()) != '(') {
                    // suffix += temp;
                    // }
                    while (!s.isEmpty()) {
                        temp = s.pop();
                        if (temp == "(") {
                            break;
                        } else {
                            st.add(temp);
                        }
                    }
                    break;
                // 默认情况，如果读取到的是数字，则直接送至输出序列
                default:
                    st.add(ch);
                    break;
            }
        }
        // 如果堆栈不为空，则把剩余运算符一次弹出，送至输出序列
        while (s.size() != 0) {
            st.add(s.pop());
        }
        return st;
    }
}
