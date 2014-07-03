import pyglet
import pyglet.media
import time

pyglet.options['audio'] = ('openal', 'silent')


window = pyglet.window.Window()

label = pyglet.text.Label('Hello, world', font_name='Times New Roman', font_size=36, x=window.width//2, y=window.height//2, anchor_x='center', anchor_y='center')
@window.event
def on_draw():
    window.clear()
    label.draw()

source = pyglet.media.load('whistle_mono_file.wav')

soundPlayer  = pyglet.media.Player()

user = pyglet.media.listener
user.position = (0,0,0)
user.forward_orientation = (0,1,0)
user.up_orientation = (0,0,1)


soundPlayer.queue(source)

soundPlayer.position = (10,0,0)

soundPlayer.play()

#source.play()

pyglet.app.run()