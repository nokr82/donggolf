/*
 * Copyright (C) 2016 Muhammed Irshad
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package donggolf.android.editor.Components;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.QuoteSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import donggolf.android.R;
import donggolf.android.editor.Components.ComponentsWrapper;
import donggolf.android.editor.Components.CustomEditText;
import donggolf.android.editor.Components.ListItemExtensions;
import donggolf.android.editor.EditorComponent;
import donggolf.android.editor.EditorCore;
import donggolf.android.editor.Utilities.FontCache;
import donggolf.android.editor.Utilities.Utilities;
import donggolf.android.editor.models.EditorContent;
import donggolf.android.editor.models.EditorTextStyle;
import donggolf.android.editor.models.EditorControl;
import donggolf.android.editor.models.EditorType;
import donggolf.android.editor.models.HtmlTag;
import donggolf.android.editor.models.Node;
import donggolf.android.editor.models.Op;
import donggolf.android.editor.models.RenderType;
import donggolf.android.editor.models.TextSettings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static donggolf.android.editor.models.TextSetting.TEXT_COLOR;

/**
 * Created by mkallingal on 4/30/2016.
 */
public class InputExtensions extends EditorComponent {
    public static final int HEADING = 0;
    public static final int CONTENT = 1;
    private String DEFAULT_TEXT_COLOR = "#000000";
    private int H1TEXTSIZE = 23;
    private int H2TEXTSIZE = 20;
    private int H3TEXTSIZE = 18;
    private int NORMALTEXTSIZE = 16;
    private int fontFace = R.string.fontFamily__serif;
    EditorCore editorCore;
    private Map<Integer, String> contentTypeface;
    private Map<Integer, String> headingTypeface;
    private float lineSpacing = -1;

    public int getH1TextSize() {
        return this.H1TEXTSIZE;
    }

    public void setH1TextSize(int size) {
        this.H1TEXTSIZE = size;
    }

    public int getH2TextSize() {
        return this.H2TEXTSIZE;
    }

    public void setH2TextSize(int size) {
        this.H2TEXTSIZE = size;
    }

    public int getH3TextSize() {
        return this.H3TEXTSIZE;
    }

    public void setH3TextSize(int size) {
        this.H3TEXTSIZE = size;
    }

    public int getNormalTextSize() {
        return this.NORMALTEXTSIZE;
    }

    public void setNormalTextSize(int size) {
        this.NORMALTEXTSIZE = size;
    }


    public void setDefaultTextColor(String color) {
        this.DEFAULT_TEXT_COLOR = color;
    }

    public String getDefaultTextColor() {
        return this.DEFAULT_TEXT_COLOR;
    }


    public String getFontFace() {
        return editorCore.getContext().getResources().getString(fontFace);
    }

    public void setFontFace(int fontFace) {
        this.fontFace = fontFace;
    }


    public Map<Integer, String> getContentTypeface() {
        return contentTypeface;
    }

    public void setContentTypeface(Map<Integer, String> contentTypeface) {
        this.contentTypeface = contentTypeface;
    }

    public Map<Integer, String> getHeadingTypeface() {
        return headingTypeface;
    }

    public void setHeadingTypeface(Map<Integer, String> headingTypeface) {
        this.headingTypeface = headingTypeface;
    }


    @Override
    public Node getContent(View view) {
        Node node = this.getNodeInstance(view);
        EditText _text = (EditText) view;
        EditorControl tag = (EditorControl) view.getTag();
        node.contentStyles = tag.editorTextStyles;
        node.content.add(Html.toHtml(_text.getText()));
        node.textSettings = tag.textSettings;
        return node;
    }

    @Override
    public String getContentAsHTML(Node node, EditorContent content) {
        String html = getInputHtml(node);
        return html;
    }

    @Override
    public void renderEditorFromState(Node node, EditorContent content) {
        String text = node.content.get(0);
        TextView view = insertEditText(editorCore.getChildCount(), editorCore.getPlaceHolder(), text);
        applyTextSettings(node, view);
    }

    @Override
    public Node buildNodeFromHTML(Element element) {
        String text;
        int count;
        TextView tv;
        HtmlTag tag = HtmlTag.valueOf(element.tagName().toLowerCase());
        switch (tag){
            case h1:
            case h2:
            case h3:
                RenderHeader(tag, element);
                break;
            case p:
            case div:
                text = element.html();
                count = editorCore.getParentView().getChildCount();
                tv = insertEditText(count, null, text);
                applyStyles(tv, element);
                break;
            case blockquote:
                text = element.html();
                count = editorCore.getParentView().getChildCount();
                tv = insertEditText(count, null, text);
                UpdateTextStyle(EditorTextStyle.BLOCKQUOTE,tv);
                applyStyles(tv, element);
        }
        return null;
    }

    @Override
    public void init(ComponentsWrapper componentsWrapper) {
        this.componentsWrapper = componentsWrapper;
    }

    public InputExtensions(EditorCore editorCore) {
        super(editorCore);
        this.editorCore = editorCore;
    }

    CharSequence GetSanitizedHtml(CharSequence text) {
        Spanned __ = Html.fromHtml(text.toString());
        CharSequence toReplace = noTrailingwhiteLines(__);
        return toReplace;
    }

    public void setText(TextView textView, CharSequence text) {
        CharSequence toReplace = GetSanitizedHtml(text);
        textView.setText(toReplace);
    }


    private TextView getNewTextView(CharSequence text) {
        final TextView textView = new TextView(new ContextThemeWrapper(this.editorCore.getContext(), R.style.WysiwygEditText));
        addEditableStyling(textView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        if (!TextUtils.isEmpty(text)) {
            Spanned __ = Html.fromHtml(text.toString());
            CharSequence toReplace = noTrailingwhiteLines(__);
            textView.setText(toReplace);
            Linkify.addLinks(textView,Linkify.ALL);
        }

        if(this.lineSpacing != -1) {
            setLineSpacing(textView, this.lineSpacing);
        }
        return textView;
    }

    public void setLineSpacing(TextView textView, float lineHeight) {
        int fontHeight = textView.getPaint().getFontMetricsInt(null);
        textView.setLineSpacing(Utilities.dpToPx(editorCore.getContext(), lineHeight)-fontHeight, 1);
    }

    public donggolf.android.editor.Components.CustomEditText getNewEditTextInst(final String hint, CharSequence text) {
        final donggolf.android.editor.Components.CustomEditText editText = new donggolf.android.editor.Components.CustomEditText(new ContextThemeWrapper(this.editorCore.getContext(), R.style.WysiwygEditText));
        addEditableStyling(editText);
        editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (hint != null) {
            editText.setHint(hint);
        }
        if (text != null) {
            setText(editText, text);
        }

        /**
         * create tag for the editor
         */

        EditorControl editorTag = editorCore.createTag(EditorType.INPUT);
        editorTag.textSettings = new TextSettings(this.DEFAULT_TEXT_COLOR);
        editText.setTag(editorTag);
        editText.setBackgroundDrawable(ContextCompat.getDrawable(this.editorCore.getContext(), R.drawable.invisible_edit_text));
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return editorCore.onKey(v, keyCode, event, editText);
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    editText.clearFocus();
                } else {
                    editorCore.setActiveView(v);
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = Html.toHtml(editText.getText());
                Object tag = editText.getTag(R.id.control_tag);
                if (s.length() == 0 && tag != null)
                    editText.setHint(tag.toString());
                if (s.length() > 0) {
                    /*
                     * if user had pressed enter, replace it with br
                     */
                    for (int i = 0; i < s.length(); i++) {
                        if (s.charAt(i) == '\n') {
                            CharSequence subChars = s.subSequence(0, i);
                            SpannableStringBuilder ssb = new SpannableStringBuilder(subChars);
                            text = Html.toHtml(ssb);
                            if (text.length() > 0)
                                setText(editText, text);


//                            if (i + 1 == s.length()) {
//                                s.clear();
//                            }

                            int index = editorCore.getParentView().indexOfChild(editText);

                            /* if the index was 0, set the placeholder to empty, behaviour happens when the user just press enter */


                            System.out.println("index:::::::::::::::::::::::::::::::::::::" + index);
                            if (index == 0) {
                                editText.setHint(null);
                                editText.setTag(R.id.control_tag, hint);
                            }

                            int position = index + 1;
                            CharSequence newText = null;
                            SpannableStringBuilder editable = new SpannableStringBuilder();

                            int lastIndex = s.length();

                            System.out.println("lastIndex:::::::::::::::::::::::::::::::::::" + lastIndex);

                            int nextIndex = i + 1;

                            System.out.println("nextIndex:::::::::::::::::::::::::::::::::::" + nextIndex);
//                            if (nextIndex < lastIndex) {
//                                newText = s.subSequence(nextIndex, lastIndex);
//                                for (int j = 0; j < newText.length(); j++) {
//                                    editable.append(newText.charAt(j));
//                                    if (newText.charAt(j) == '\n') {
//                                        editable.append('\n');
//                                    }
//                                }
//                            }
                            insertEditText(position, hint, editable);
                            break;
                        }
                    }
                }
                if (editorCore.getEditorListener() != null) {
                    editorCore.getEditorListener().onTextChanged(editText, s);
                }
            }
        });
        if(this.lineSpacing != -1) {
            setLineSpacing(editText, this.lineSpacing);
        }
        return editText;
    }

    private boolean isLastText(int index) {
        if (index == 0)
            return false;
        View view = editorCore.getParentView().getChildAt(index - 1);
        EditorType type = editorCore.getControlType(view);
        return type == EditorType.INPUT;
    }

    private void addEditableStyling(TextView editText) {
        editText.setTypeface(getTypeface(CONTENT, Typeface.NORMAL));
        editText.setFocusableInTouchMode(true);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, NORMALTEXTSIZE);
        editText.setTextColor(Color.parseColor(this.DEFAULT_TEXT_COLOR));
        editText.setPadding(0,30,0,30);
    }



    public TextView insertEditText(int position, String hint, CharSequence text) {
        String nextHint = isLastText(position) ? null : editorCore.getPlaceHolder();
        if (editorCore.getRenderType() == RenderType.Editor) {
            /**
             * when user press enter from first line without keyin anything, need to remove the placeholder from that line 0...
             */
            if (position == 1) {
                View view = editorCore.getParentView().getChildAt(0);
                EditorType type = editorCore.getControlType(view);
                if (type == EditorType.INPUT) {
                    TextView textView = (TextView) view;
                    if (TextUtils.isEmpty(textView.getText())) {
                        textView.setHint(null);
                    }
                }
            }

            final donggolf.android.editor.Components.CustomEditText view = getNewEditTextInst(nextHint, text);
            editorCore.getParentView().addView(view, position);
            editorCore.setActiveView(view);
            final android.os.Handler handler = new android.os.Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFocus(view);
                }
            }, 0);
            editorCore.setActiveView(view);
            return view;
        } else {
            final TextView view = getNewTextView(text);
            view.setTag(editorCore.createTag(EditorType.INPUT));
            editorCore.getParentView().addView(view);
            return view;
        }
    }


    private EditorControl reWriteTags(EditorControl tag, EditorTextStyle styleToAdd) {
        EditorTextStyle[] tags = {EditorTextStyle.H1,EditorTextStyle.H2,EditorTextStyle.H3,EditorTextStyle.NORMAL};
        for(EditorTextStyle style: tags)
            tag = editorCore.updateTagStyle(tag, style, Op.Delete);
        tag = editorCore.updateTagStyle(tag, styleToAdd, Op.Insert);
        return tag;
    }

    public boolean isEditorTextStyleHeaders(EditorTextStyle editorTextStyle) {
        return editorTextStyle == EditorTextStyle.H1 || editorTextStyle == EditorTextStyle.H2 || editorTextStyle == EditorTextStyle.H3;
    }

    public boolean isEditorTextStyleContentStyles(EditorTextStyle editorTextStyle) {
        return editorTextStyle == EditorTextStyle.BOLD || editorTextStyle == EditorTextStyle.BOLDITALIC || editorTextStyle == EditorTextStyle.ITALIC;
    }


    public int getTextStyleFromStyle(EditorTextStyle editorTextStyle) {
        if (editorTextStyle == EditorTextStyle.H1)
            return H1TEXTSIZE;
        if (editorTextStyle == EditorTextStyle.H2)
            return H2TEXTSIZE;
        if (editorTextStyle == EditorTextStyle.H3)
            return H3TEXTSIZE;
        return NORMALTEXTSIZE;
    }

    private void updateTextStyle(TextView editText, EditorTextStyle editorTextStyle) {
        EditorControl tag;
        if (editText == null) {
            editText = (EditText) editorCore.getActiveView();
        }
        EditorControl editorControl = editorCore.getControlTag(editText);
        if (isEditorTextStyleHeaders(editorTextStyle)) {
            if (editorCore.containsStyle(editorControl.editorTextStyles, editorTextStyle)) {
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, NORMALTEXTSIZE);
                editText.setTypeface(getTypeface(CONTENT, Typeface.NORMAL));
                tag = reWriteTags(editorControl, EditorTextStyle.NORMAL);
            } else {
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextStyleFromStyle(editorTextStyle));
                editText.setTypeface(getTypeface(HEADING, Typeface.BOLD));
                tag = reWriteTags(editorControl, editorTextStyle);
            }
            editText.setTag(tag);
        }
    }

    private boolean containsHeaderTextStyle(EditorControl tag) {
        for (EditorTextStyle item : tag.editorTextStyles) {
            if (isEditorTextStyleHeaders(item)) {
                return true;
            }
            continue;
        }
        return false;
    }


    public void boldifyText(EditorControl tag, TextView editText, int textMode) {
        if (editorCore.containsStyle(tag.editorTextStyles, EditorTextStyle.BOLD)) {
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.BOLD, Op.Delete);
            editText.setTypeface(getTypeface(textMode, Typeface.NORMAL));
        } else if (editorCore.containsStyle(tag.editorTextStyles, EditorTextStyle.BOLDITALIC)) {
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.BOLDITALIC, Op.Delete);
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.ITALIC, Op.Insert);
            editText.setTypeface(getTypeface(textMode, Typeface.ITALIC));
        } else if (editorCore.containsStyle(tag.editorTextStyles, EditorTextStyle.ITALIC)) {
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.BOLDITALIC, Op.Insert);
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.ITALIC, Op.Delete);
            editText.setTypeface(getTypeface(textMode, Typeface.BOLD_ITALIC));
        } else {
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.BOLD, Op.Insert);
            editText.setTypeface(getTypeface(textMode, Typeface.BOLD));
        }
        editText.setTag(tag);
    }

    public void italicizeText(EditorControl tag, TextView editText, int textMode) {

        if (editorCore.containsStyle(tag.editorTextStyles, EditorTextStyle.ITALIC)) {
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.ITALIC, Op.Delete);
            editText.setTypeface(getTypeface(textMode, Typeface.NORMAL));
        } else if (editorCore.containsStyle(tag.editorTextStyles, EditorTextStyle.BOLDITALIC)) {
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.BOLDITALIC, Op.Delete);
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.BOLD, Op.Insert);
            editText.setTypeface(getTypeface(textMode, Typeface.BOLD));
        } else if (editorCore.containsStyle(tag.editorTextStyles, EditorTextStyle.BOLD)) {
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.BOLDITALIC, Op.Insert);
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.BOLD, Op.Delete);
            editText.setTypeface(getTypeface(textMode, Typeface.BOLD_ITALIC));
        } else {
            tag = editorCore.updateTagStyle(tag, EditorTextStyle.ITALIC, Op.Insert);
            editText.setTypeface(getTypeface(textMode, Typeface.ITALIC));
        }
        editText.setTag(tag);
    }

    public void UpdateTextStyle(EditorTextStyle style, TextView editText) {
        /// String type = getControlType(getActiveView());
        try {
            if (editText == null) {
                editText = (EditText) editorCore.getActiveView();
            }
            EditorControl tag = editorCore.getControlTag(editText);

            int pBottom = editText.getPaddingBottom();
            int pRight = editText.getPaddingRight();
            int pTop = editText.getPaddingTop();



            if (isEditorTextStyleHeaders(style)) {
                updateTextStyle(editText, style);
                return;
            }
            if (isEditorTextStyleContentStyles(style)) {
                boolean containsHeadertextStyle = containsHeaderTextStyle(tag);
                if (style == EditorTextStyle.BOLD) {
                    boldifyText(tag, editText, containsHeadertextStyle ? HEADING : CONTENT);
                } else if (style == EditorTextStyle.ITALIC) {
                    italicizeText(tag, editText, containsHeadertextStyle ? HEADING : CONTENT);
                }
                return;
            }
            if (style == EditorTextStyle.INDENT) {
                if (editorCore.containsStyle(tag.editorTextStyles, EditorTextStyle.INDENT)) {
                    tag = editorCore.updateTagStyle(tag, EditorTextStyle.INDENT, Op.Delete);
                    editText.setPadding(0, pTop, pRight, pBottom);
                    editText.setTag(tag);
                } else {
                    tag = editorCore.updateTagStyle(tag, EditorTextStyle.INDENT, Op.Insert);
                    editText.setPadding(30, pTop, pRight, pBottom);
                    editText.setTag(tag);
                }
            } else if (style == EditorTextStyle.OUTDENT) {
                if (editorCore.containsStyle(tag.editorTextStyles, EditorTextStyle.INDENT)) {
                    tag = editorCore.updateTagStyle(tag, EditorTextStyle.INDENT, Op.Delete);
                    editText.setPadding(0, pTop, pRight, pBottom);
                    editText.setTag(tag);
                }
            } else if( style == EditorTextStyle.BLOCKQUOTE){
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) editText.getLayoutParams();
                if (editorCore.containsStyle(tag.editorTextStyles, EditorTextStyle.BLOCKQUOTE)) {
                    tag = editorCore.updateTagStyle(tag, EditorTextStyle.BLOCKQUOTE, Op.Delete);
                    editText.setPadding(0, pTop, pRight, pBottom);
                    editText.setBackgroundDrawable(ContextCompat.getDrawable(this.editorCore.getContext(), R.drawable.invisible_edit_text));
                    params.setMargins(0, 0, 0, (int) editorCore.getContext().getResources().getDimension(R.dimen.edittext_margin_bottom));
                }else{
                    float marginExtra =  editorCore.getContext().getResources().getDimension(R.dimen.edittext_margin_bottom)*1.5f;
                    tag = editorCore.updateTagStyle(tag, EditorTextStyle.BLOCKQUOTE, Op.Insert);
                    editText.setPadding(30, pTop, 30, pBottom);
                    editText.setBackgroundDrawable(editText.getContext().getResources().getDrawable(R.drawable.block_quote_background));
                    params.setMargins(0, (int)marginExtra, 0, (int) marginExtra);
                }
                editText.setTag(tag);
            }
        } catch (Exception e) {

        }
    }



    public void insertLink() {
        final AlertDialog.Builder inputAlert = new AlertDialog.Builder(this.editorCore.getContext());
        inputAlert.setTitle("Add a Link");
        final EditText userInput = new EditText(this.editorCore.getContext());
        //dont forget to add some margins on the left and right to match the title
        userInput.setHint("type the URL here");
        userInput.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
        inputAlert.setView(userInput);
        inputAlert.setPositiveButton("Insert", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInputValue = userInput.getText().toString();
                insertLink(userInputValue);
            }
        });
        inputAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = inputAlert.create();
        alertDialog.show();
    }

    public void appendText(Editable text) {

    }

    public void insertLink(String uri) {
        EditorType editorType = editorCore.getControlType(editorCore.getActiveView());
        EditText editText = (EditText) editorCore.getActiveView();
        if (editorType == EditorType.INPUT || editorType == EditorType.UL_LI) {
            String text = Html.toHtml(editText.getText());
            if (TextUtils.isEmpty(text))
                text = "<p dir=\"ltr\"></p>";
            text = trimLineEnding(text);
            Document _doc = Jsoup.parse(text);
            Elements x = _doc.select("p");
            String existing = x.get(0).html();
            x.get(0).html(existing + " <a href='" + uri + "'>" + uri + "</a>");
            Spanned toTrim = Html.fromHtml(x.toString());
            CharSequence trimmed = noTrailingwhiteLines(toTrim);
            editText.setText(trimmed);   //
            editText.setSelection(editText.getText().length());
        }
    }

    public CharSequence noTrailingwhiteLines(CharSequence text) {
        if (text.length() == 0)
            return text;
        while (text.charAt(text.length() - 1) == '\n') {
            text = text.subSequence(0, text.length() - 1);
        }
        return text;
    }

    public CharSequence noLeadingwhiteLines(CharSequence text) {
        if (text.length() == 0)
            return text;
        while (text.charAt(0) == '\n') {
            text = text.subSequence(1, text.length());
        }
        return text;
    }

    public boolean isEditTextEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    private String trimLineEnding(String s) {
        if (s.charAt(s.length() - 1) == '\n') {
            String formatted = s.toString().substring(0, s.length() - 1);
            return formatted;
        }
        return s;
    }

    /**
     * returns the appropriate typeface
     *
     * @param mode  => whether heading (0) or content(1)
     * @param style => NORMAL, BOLD, BOLDITALIC, ITALIC
     * @return typeface
     */
    public Typeface getTypeface(int mode, int style) {
        if (mode == HEADING && headingTypeface == null) {
            return Typeface.create(getFontFace(), style);
        } else if (mode == CONTENT && contentTypeface == null) {
            return Typeface.create(getFontFace(), style);
        }
        if (mode == HEADING && !headingTypeface.containsKey(style)) {
            throw new IllegalArgumentException("the provided fonts for heading is missing the varient for this style. Please checkout the documentation on adding custom fonts.");
        } else if (mode == CONTENT && !headingTypeface.containsKey(style)) {
            throw new IllegalArgumentException("the provided fonts for content is missing the varient for this style. Please checkout the documentation on adding custom fonts.");
        }
        if (mode == HEADING) {
            return FontCache.get(headingTypeface.get(style), editorCore.getContext());
        } else {
            return FontCache.get(contentTypeface.get(style), editorCore.getContext());
        }
    }

    public void setFocus(donggolf.android.editor.Components.CustomEditText view) {
        if (editorCore.isStateFresh() && !editorCore.getAutoFucus()) {
            editorCore.setStateFresh(false);
            return;
        }
        view.requestFocus();
        InputMethodManager mgr = (InputMethodManager) editorCore.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        view.setSelection(view.getText().length());
        editorCore.setActiveView(view);
    }


    public donggolf.android.editor.Components.CustomEditText getEditTextPrevious(int startIndex) {
        donggolf.android.editor.Components.CustomEditText customEditText = null;
        for (int i = 0; i < startIndex; i++) {
            View view = editorCore.getParentView().getChildAt(i);
            EditorType editorType = editorCore.getControlType(view);
            if (editorType == EditorType.hr || editorType == EditorType.img || editorType == EditorType.map )
                continue;
            if (editorType == EditorType.INPUT) {
                customEditText = (donggolf.android.editor.Components.CustomEditText) view;
                continue;
            }
            if (editorType == EditorType.ol || editorType == EditorType.ul) {
                componentsWrapper.getListItemExtensions().setFocusToList(view, ListItemExtensions.POSITION_START);
                editorCore.setActiveView(view);
            }
        }
        return customEditText;
    }

    public void setFocusToPrevious(int startIndex) {
        for (int i = startIndex; i > 0; i--) {
            View view = editorCore.getParentView().getChildAt(i);
            EditorType editorType = editorCore.getControlType(view);
            if (editorType == EditorType.hr || editorType == EditorType.img || editorType == EditorType.map)
                continue;
            if (editorType == EditorType.INPUT) {
                setFocus((donggolf.android.editor.Components.CustomEditText) view);
                break;
            }
            if (editorType == EditorType.ol || editorType == EditorType.ul) {
                componentsWrapper.getListItemExtensions().setFocusToList(view, ListItemExtensions.POSITION_START);
                editorCore.setActiveView(view);
            }
        }
    }

    public boolean isInputTextAtPosition(int position){
        return editorCore.getControlType(editorCore.getParentView().getChildAt(position)) == EditorType.INPUT;
    }



    public void updateTextColor(String color, TextView editText) {
        try {

            if(color.contains("rgb")){
                Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
                Matcher m = c.matcher(color);
                if(m. matches()) {
                    int r = Integer.parseInt(m.group(1));
                    int g = Integer.parseInt(m.group(2));
                    int b = Integer.parseInt(m.group(3));
                    color = String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b);
                }
            }

            if (editText == null) {
                editText = (EditText) editorCore.getActiveView();
            }
            EditorControl tag = editorCore.getControlTag(editText);
            if(tag.textSettings==null)
                tag.textSettings = new TextSettings(color);
            else
                tag.textSettings.setTextColor(color);
            editText.setTag(tag);
            editText.setTextColor(Color.parseColor(color));
        } catch (Exception ex) {
            Log.e(editorCore.TAG, ex.getMessage());
        }
    }

    public void applyStyles(TextView editText, Element element) {
        Map<String, String> styles = componentsWrapper.getHtmlExtensions().getStyleMap(element);
        if(styles.containsKey("color")){
            updateTextColor(styles.get("color"),editText);
        }
    }

    public String getInputHtml(Node item) {
        boolean isParagraph = true;
        String tmpl = componentsWrapper.getHtmlExtensions().getTemplateHtml(item.type);
        //  CharSequence content= android.text.Html.fromHtml(item.content.get(0)).toString();
        //  CharSequence trimmed= editorCore.getInputExtensions().noTrailingwhiteLines(content);
        String trimmed = Jsoup.parse(item.content.get(0)).body().select("p").html();
        Map<Enum, String> styles = new HashMap<>();
        if (item.contentStyles.size() > 0) {
            for (EditorTextStyle style : item.contentStyles) {
                switch (style) {
                    case BOLD:
                        tmpl = tmpl.replace("{{$content}}", "<b>{{$content}}</b>");
                        break;
                    case BOLDITALIC:
                        tmpl = tmpl.replace("{{$content}}", "<b><i>{{$content}}</i></b>");
                        break;
                    case ITALIC:
                        tmpl = tmpl.replace("{{$content}}", "<i>{{$content}}</i>");
                        break;
                    case INDENT:
                        styles.put(style, "margin-left:25px");
                        break;
                    case OUTDENT:
                        styles.put(style, "margin-left:0");
                        break;
                    case H1:
                        tmpl = tmpl.replace("{{$tag}}", "h1");
                        isParagraph = false;
                        break;
                    case H2:
                        tmpl = tmpl.replace("{{$tag}}", "h2");
                        isParagraph = false;
                        break;
                    case H3:
                        tmpl = tmpl.replace("{{$tag}}", "h3");
                        isParagraph = false;
                        break;
                    case BLOCKQUOTE:
                        tmpl = tmpl.replace("{{$tag}}", "blockquote");
                        isParagraph = false;
                        break;
                    case NORMAL:
                        tmpl = tmpl.replace("{{$tag}}", "p");
                        isParagraph = true;
                        break;
                }
            }
        }

        styles.put(TEXT_COLOR, "color:" + item.textSettings.getTextColor());

        if (item.type == EditorType.OL_LI || item.type == EditorType.UL_LI) {
            tmpl = tmpl.replace("{{$tag}}", "span");
        } else if (isParagraph) {
            tmpl = tmpl.replace("{{$tag}}", "p");
        }
        tmpl = tmpl.replace("{{$content}}", trimmed);
        tmpl = tmpl.replace(" {{$style}}", createStyleTag(styles));
        return tmpl;
    }

    private String createStyleTag(Map<Enum, String> styles) {
        String tmpl = " style=\"{{builder}}\"";
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Enum, String> style : styles.entrySet()) {
            builder.append(style.getValue()).append(";");
        }
        tmpl = tmpl.replace("{{builder}}", builder);
        return tmpl;
    }

    public void applyTextSettings(Node node, TextView view){
        if (node.contentStyles != null) {
            for (EditorTextStyle style : node.contentStyles) {
                UpdateTextStyle(style, view);
            }

            if(!TextUtils.isEmpty(node.textSettings.getTextColor())) {
                updateTextColor(node.textSettings.getTextColor(), view);
            }
        }
    }

    public void RenderHeader(HtmlTag tag, Element element) {
        int count = editorCore.getParentView().getChildCount();
        String text = componentsWrapper.getHtmlExtensions().getHtmlSpan(element);
        TextView editText = insertEditText(count, null, text);
        EditorTextStyle style = tag == HtmlTag.h1 ? EditorTextStyle.H1 : tag == HtmlTag.h2 ? EditorTextStyle.H2 : EditorTextStyle.H3;
        UpdateTextStyle(style, editText);
        applyStyles(editText, element);
    }

    public void removeFocus(CustomEditText editText) {
        editText.clearFocus();
        InputMethodManager imm = (InputMethodManager) editorCore.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        editorCore.getParentView().removeView(editText);
    }

    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
    }
}
