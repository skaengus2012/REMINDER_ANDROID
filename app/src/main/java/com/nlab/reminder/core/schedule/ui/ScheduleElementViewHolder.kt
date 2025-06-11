/*
 * Copyright (C) 2024 The N's lab Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nlab.reminder.core.schedule.ui


/**
 * @author Doohyun
 */
/**
internal class ScheduleElementViewHolder(
    internal val binding: ViewItemScheduleElementBinding,
    selectionEnabled: Flow<Boolean>,
    eventListener: ScheduleElementItemEventListener
) : ScheduleItemViewHolder(binding.root) {
    private val linkThumbnailPlaceHolderDrawable: Drawable? = with(itemView) {
        AppCompatResources.getDrawable(context, R.drawable.ic_schedule_link_error)
            ?.let(DrawableCompat::wrap)
            ?.apply {
                DrawableCompat.setTint(
                    mutate(),
                    context.getThemeColor(com.nlab.reminder.core.designsystem.R.attr.content_2)
                )
            }
    }
    private val layoutContentNormalSet: ConstraintSet =
        ConstraintSet().apply { clone(binding.layoutContent) }
    private val layoutContentSelectionSet: ConstraintSet =
        ConstraintSet().apply { load(itemView.context, R.layout.view_item_schedule_element_content_selected) }

    init {
        binding.initWithLifecycleOwner { lifecycleOwner ->
            buttonComplete
                .throttleClicks()
                .withItemPosition()
                .onEach { (view, position) ->
                    eventListener.onCompleteClicked(position, view.isSelected.not())
                }
                .launchIn(lifecycleOwner.lifecycleScope)

            layoutDelete
                .throttleClicks()
                .mapToItemPosition()
                .onEach(eventListener::onDeleteClicked)
                .launchIn(lifecycleOwner.lifecycleScope)

            cardLink
                .throttleClicks()
                .mapToItemPosition()
                .onEach(eventListener::onLinkClicked)
                .launchIn(lifecycleOwner.lifecycleScope)

            buttonDragHandle
                .touches()
                .filter { event -> event.action == MotionEvent.ACTION_DOWN }
                .onEach { eventListener.onDragHandleClicked(viewHolder = this@ScheduleElementViewHolder) }
                .launchIn(lifecycleOwner.lifecycleScope)

            buttonSelection
                .touches()
                .filter { event -> event.action == MotionEvent.ACTION_DOWN }
                .onEach {
                    eventListener.onSelectTouched(
                        absolutePosition = absoluteAdapterOptionalPosition ?: return@onEach,
                        isSelected = buttonSelection.isSelected.not()
                    )
                }
                .launchIn(lifecycleOwner.lifecycleScope)

            selectionEnabled
                .flowWithLifecycle(lifecycleOwner.lifecycle)
                .onEach { isSelectionMode -> buttonSelection.isEnabled = isSelectionMode }
                .onEach { isSelectionMode -> buttonComplete.isEnabled = isSelectionMode.not() }
                .onEach { isSelectionMode ->
                    TransitionManager.beginDelayedTransition(
                        layoutContent,
                        AutoTransition().apply {
                            duration = 300
                            addListener(transitionListenerOf(
                                onStart = { layoutDelete.visibility = View.INVISIBLE },
                                onEnd = { layoutDelete.visibility = View.VISIBLE },
                                onCancel = { layoutDelete.visibility = View.VISIBLE }
                            ))
                        }
                    )
                    val applyConstraintSet: ConstraintSet =
                        if (isSelectionMode) layoutContentSelectionSet else layoutContentNormalSet
                    applyConstraintSet.applyTo(layoutContent)
                }
                .launchIn(lifecycleOwner.lifecycleScope)
        }
    }

    private fun Flow<View>.withItemPosition(): Flow<Pair<View, Int>> = mapNotNull { v ->
        val position = bindingAdapterOptionalPosition
        if (position == null) null
        else v to position
    }

    private fun Flow<*>.mapToItemPosition(): Flow<Int> = mapNotNull { bindingAdapterOptionalPosition }

    fun onBind(scheduleElement: ScheduleElement) {
        val isTitleChanged: Boolean

        binding.edittextTitle
            .bindText(scheduleElement.title)
            .also { isTitleChanged = it }
        binding.edittextNote.bindText(scheduleElement.note)
        binding.buttonComplete.apply {
            bindSelected(scheduleElement.isCompleteMarked)
            if (isTitleChanged) {
                contentDescription = scheduleElement.completeButtonDescription(context)
            }
        }
        binding.buttonSelection.apply {
            val isChanged = bindSelected(scheduleElement.isSelected)
            if (isChanged) {
                contentDescription = context.getString(
                    if (scheduleElement.isSelected) R.string.schedule_selection_checkbox_undo_contentDescription
                    else R.string.schedule_selection_checkbox_contentDescription,
                    scheduleElement.title
                )
            }
        }

        // link binding
        binding.cardLink.visibility = if (scheduleElement.link.isEmpty()) View.GONE else View.VISIBLE
        binding.textviewLink.bindText(scheduleElement.link.value)
        binding.textviewTitleLink.apply {
            val linkMetadataTitle = scheduleElement.linkMetadata?.title
            visibility = if (linkMetadataTitle.isNullOrBlank()) View.GONE else View.VISIBLE
            bindText(linkMetadataTitle)
        }
        binding.imageviewBgLinkThumbnail.apply {
            val linkMetadataImage = scheduleElement.linkMetadata?.imageUrl
            visibility = if (linkMetadataImage.isNullOrBlank()) View.GONE else View.VISIBLE
            bindLinkMetadataImage(linkMetadataImage, linkThumbnailPlaceHolderDrawable)
        }
    }
}

private fun ScheduleElement.completeButtonDescription(context: Context): String =
    context.getString(
        if (isCompleteMarked) R.string.schedule_complete_checkbox_undo_contentDescription
        else R.string.schedule_complete_checkbox_contentDescription,
        title
    )

private fun ImageView.bindLinkMetadataImage(newImage: String?, placeHolder: Drawable?) {
    val oldImage: String? = tag as? String
    if (oldImage == newImage) return
    tag = newImage
    load(newImage) {
        placeholder(placeHolder)
        error(placeHolder)
    }
}*/