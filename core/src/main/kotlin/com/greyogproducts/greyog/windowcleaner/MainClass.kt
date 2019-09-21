package com.greyogproducts.greyog.windowcleaner

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import ktx.graphics.use
import ktx.log.info

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirstScreen : KtxScreen {
    private val image = Texture("ktx-logo.png")
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera()
    private val viewport = ScreenViewport(camera)
    private val stage = Stage(viewport)
    private val backPixMap : Pixmap  by lazy { Pixmap(stage.width.toInt(), stage.height.toInt(), Pixmap.Format.RGB888) }
    private val backTexture : Texture by lazy { Texture(backPixMap, true) }

    init {
        KtxAsync.initiate()
    }

    private fun updateBackTexture(): Unit {
        backTexture.draw(backPixMap, 0, 0)
        backTexture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest)
    }

    override fun show() {
        super.show()
        stage.isDebugAll = true
        batch.projectionMatrix = camera.combined
        Gdx.input.inputProcessor = stage
//        val windo = Okno()
//        windo.setBounds(10f, 20f, 300f, 200f)
//        stage.addActor(windo)

        backPixMap.setColor(Color.CORAL)
        backPixMap.filter = Pixmap.Filter.NearestNeighbour
        backPixMap.fill()
        updateBackTexture()
        val skliz = Skliz(backPixMap)
        stage.addActor(skliz)
        skliz.x = stage.width/2
        skliz.y = stage.height/2
        stage.addListener(object : ActorGestureListener() {
            private var startX = 0f
            private var startY = 0f
            private var isMoving = false
            override fun zoom(event: InputEvent?, initialDistance: Float, distance: Float) {
                super.zoom(event, initialDistance, distance)
//                info { "ZOOOOM!!!!" }
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                isMoving = false
                lastAng = 0f
                lastP1 = null
                super.touchUp(event, x, y, pointer, button)
            }

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                isMoving = true
                startX = x
                startY = y
                skliz.canDrawTrace = true
                super.touchDown(event, x, y, pointer, button)
            }

            override fun pan(event: InputEvent?, x: Float, y: Float, deltaX: Float, deltaY: Float) {
                skliz.moveBy(deltaX, deltaY)
                super.pan(event, x, y, deltaX, deltaY)
            }

            private var lastAng = 0f
            private var lastP1 : Vector2? = null
            private val p = Vector2()

            override fun pinch(event: InputEvent?, initialPointer1: Vector2, initialPointer2: Vector2, pointer1: Vector2, pointer2: Vector2) {
                val a = Vector2(initialPointer2.x-initialPointer1.x, initialPointer2.y-initialPointer1.y)
                val b = Vector2(pointer2.x - pointer1.x, pointer2.y - pointer1.y)

//                info {
//                    "initialP1 = $initialPointer1   ;   pointer1 = $pointer1 "
//                }
//                if (lastP1 == null) lastP1 = Vector2(pointer1) else lastP1?.set(pointer1)
                p.set((pointer1.x + pointer2.x)/2, (pointer1.y + pointer2.y)/2)
                if (lastP1 != null) {
                    skliz.moveBy(p.x - lastP1!!.x, p.y - lastP1!!.y)
                    lastP1!!.set(p)
                } else lastP1 = Vector2(p)

                val ang = a.angle(b)
                skliz.rotateBy(ang - lastAng)
                lastAng = ang
                
                super.pinch(event, initialPointer1, initialPointer2, pointer1, pointer2)
            }
        })

        stage.addListener {
            if (it is UpdateTextureEvent) {
                updateBackTexture()
                return@addListener true
            }
            return@addListener false
        }

    }

    override fun render(delta: Float) {
        clearScreen(0.3f, 0.3f, 0.3f)
        stage.act(delta)
        batch.use {
            it.draw(backTexture, 0f, 0f)
        }
        stage.draw()
    }

    override fun dispose() {
        image.dispose()
        batch.dispose()
        backPixMap.dispose()
        backTexture.dispose()
    }
}

class MainClass : KtxGame<KtxScreen>() {
    override fun create() {
        addScreen(FirstScreen())
        setScreen<FirstScreen>()
    }
}

class Okno : Actor() {
    private val shapeRenderer = ShapeRenderer()

    init {
        shapeRenderer.color = Color.BLUE
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch?.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)

        shapeRenderer.rect(x, y, width, height)

        shapeRenderer.end()
        batch?.begin()
    }
}

class Skliz(private val pixmap: Pixmap) : Actor() {
    private val bladeLength = 150f
    private val handleLength = 50f
    private val bladeThickness = 10f
    private val handleThickness = 20f
    private val image = Texture("skliz.png")
    private val textureRegion = TextureRegion(image)
    private val colorClean = Color.BLUE
    private val colorDirty = Color.BROWN
    var canDrawTrace = false
    private var centerX = 0f
    private var centerY = 0f

    init {
        setBounds(x, y, bladeLength, bladeThickness+handleLength)
        setOrigin(x + bladeLength/2, y+height)
//        setOrigin(x , y+height)

//        centerX = x + bladeLength/2
//        centerY = y+height
//        addListener(object : InputListener(){
//            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
//                rotateBy(5f)
//                return super.touchDown(event, x, y, pointer, button)
//            }
//        })
        
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
//        batch?.draw(textureRegion,
//                x, y,
//                originX, originY,
//                width, height,
//                1f, 1f,
//                rotation)
    }

    private val lastA = Vector2()
    private val lastB = Vector2()

    override fun positionChanged() {
        super.positionChanged()
        if (lastA.isZero) rememberLastPosition()
        drawPositionChanges(false)
    }

    override fun rotationChanged() {
        super.rotationChanged()
        if (lastA.isZero) rememberLastPosition()
        drawPositionChanges(true)
    }

    private fun rememberLastPosition() {
        if (!canDrawTrace) return
        lastA.set(localToStageCoordinates(Vector2(0f, height)))
        lastB.set(localToStageCoordinates(Vector2(width, height)))
    }

    private var lastTraceDrawTime: Long = 0
    private val traceDrawPeriod = (Gdx.graphics.deltaTime * 1000).toInt()

    private val zeroError = 0.0f

    private fun drawPositionChanges(isRotation: Boolean) {
        if (!canDrawTrace) return
        if (System.currentTimeMillis() - lastTraceDrawTime < traceDrawPeriod) return
        lastTraceDrawTime = System.currentTimeMillis()

        val a0 = lastA
        val b0 = lastB

        val a1 = localToStageCoordinates(Vector2(0f, height))
        val b1 = localToStageCoordinates(Vector2(width, height))

        val posA = Intersector.pointLineSide(a0, b0, a1)
        val posB = Intersector.pointLineSide(a0, b0, b1)

        var hasDrown = false
        when {
            posA >0 && posB > 0 -> {
                drawTrianglesCase1(colorDirty, a0, b0, a1, b1)
//                drawingQueue.add(DrawingData(colorClean, a0, b0, a1, b1, null))
                info { "case1" }
                hasDrown = true
            }
            posA < 0 && posB < 0 -> {
                drawTrianglesCase1(colorClean, a0, b0, a1, b1)
//                drawingQueue.add(DrawingData(colorDirty, a0, b0, a1, b1, null))
                info { "case2" }
                hasDrown = true
            }
            posA < 0 && posB > 0 -> {
                val o = Vector2()
                val result = Intersector.intersectLines(a0, b0, a1, b1, o)
//                if (result)
                drawTrianglesCase2(colorClean, colorDirty, a0, a1, b1, b0, o)
                info { "case3" }
                hasDrown = true
            }
            posA > 0 && posB < 0 -> {
                val o = Vector2()
                val result = Intersector.intersectLines(a0, b0, a1, b1, o)
//                if (result)
                drawTrianglesCase2(colorDirty, colorClean, a0, a1, b1, b0, o)
                info { "case4" }
                hasDrown = true
            }
            else -> info{"NULLLLL"}
        }

        if (hasDrown) {
            fire(UpdateTextureEvent())
            rememberLastPosition()

            eraseMistakes(a0, a1, b0, b1)
        }
    }

    private fun eraseMistakes(a0: Vector2, a1: Vector2, b0: Vector2, b1: Vector2) {
        val executor = newSingleThreadAsyncContext()
        KtxAsync.launch {
            println("Before async: ${Thread.currentThread()}")
            withContext(executor) {
                println("During async: ${Thread.currentThread()}")
                val boxX0 = minOf(minOf(a0.x, a1.x), minOf(b0.x, b1.x)).toInt() - 10
                val boxY0 = minOf(minOf(a0.y, a1.y), minOf(b0.y, b1.y)).toInt() - 10
                val boxX1 = maxOf(maxOf(a0.x, a1.x), maxOf(b0.x, b1.x)).toInt() + 10
                val boxY1 = maxOf(maxOf(a0.y, a1.y), maxOf(b0.y, b1.y)).toInt() + 10
                val boxWidth = boxX1 - boxX0
                val boxHeight = boxY1 - boxY0
                for (a in boxX0..boxX1) {
                    for (b in boxY0..boxY1) {
                        val pixelColor = pixmap.getPixel(a, pixmap.height - b)
                        val pixelColorR = pixmap.getPixel(a + 1, pixmap.height - b)
                        val pixelColorL = pixmap.getPixel(a - 1, pixmap.height - b)
                        if (pixelColorL == pixelColorR && pixelColor != pixelColorL) {
                            pixmap.drawPixel(a, pixmap.height - b, pixelColorL)
                        }
                        val pixelColorU = pixmap.getPixel(a, pixmap.height - b + 1)
                        val pixelColorD = pixmap.getPixel(a, pixmap.height - b - 1)
                        if (pixelColorU == pixelColorD && pixelColor != pixelColorD) {
                            pixmap.drawPixel(a, pixmap.height - b, pixelColorD)
                        }
                    }
                }
            }
            println("After async:  ${Thread.currentThread()}")
        }
    }

    private fun drawTrianglesCase2(color1: Color, color2: Color, a0: Vector2, a1: Vector2, b1: Vector2, b0: Vector2, o: Vector2) {
        pixmap.setColor(color1)

        pixmap.fillTriangle(a0.x.toInt(), pixmap.height - a0.y.toInt(),
                a1.x.toInt(), pixmap.height - a1.y.toInt(),
                o.x.toInt(), pixmap.height - o.y.toInt())
        pixmap.setColor(color2)
        pixmap.fillTriangle(b0.x.toInt(), pixmap.height - b0.y.toInt(),
                b1.x.toInt(), pixmap.height - b1.y.toInt(),
                o.x.toInt(), pixmap.height - o.y.toInt())
    }

    private fun drawTrianglesCase1(color: Color, a0: Vector2, b0: Vector2, a1: Vector2, b1: Vector2) {
        pixmap.setColor(color)
        pixmap.fillTriangle(a0.x.toInt(), pixmap.height - a0.y.toInt(),
                b0.x.toInt(), pixmap.height - b0.y.toInt(),
                b1.x.toInt(), pixmap.height - b1.y.toInt())
        pixmap.fillTriangle(a0.x.toInt(), pixmap.height - a0.y.toInt(),
                a1.x.toInt(), pixmap.height - a1.y.toInt(),
                b1.x.toInt(), pixmap.height - b1.y.toInt())

        pixmap.fillTriangle(a0.x.toInt(), pixmap.height - a0.y.toInt(),
                b0.x.toInt(), pixmap.height - b0.y.toInt(),
                a1.x.toInt(), pixmap.height - a1.y.toInt())

        pixmap.fillTriangle(a1.x.toInt(), pixmap.height - a1.y.toInt(),
                b0.x.toInt(), pixmap.height - b0.y.toInt(),
                b1.x.toInt(), pixmap.height - b1.y.toInt())
    }

    private val drawingQueue = emptyList<DrawingData>().toMutableList()

    data class DrawingData(var color: Color, val a0: Vector2, val b0: Vector2, val a1: Vector2, val b1: Vector2, val o: Vector2?)
}


class UpdateTextureEvent : Event()
class StartDrawingEvent : Event()
