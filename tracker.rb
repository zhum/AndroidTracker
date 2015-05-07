#!/usr/bin/env ruby
# encoding: utf-8

require 'rubygems'
require 'json'
require 'sinatra'
require 'data_mapper'
require 'sinatra/base'
require 'sinatra/reloader'
require 'slim'

require 'pp'

#require 'newrelic_rpm'

use Rack::Session::Cookie, :key => 'session',
                           :expire_after => 2592000, # In seconds
                           :secret => 'ddkrtufedrt'
enable :sessions
enable :logging
#set :session_secret, "qqaassddrrttgg"
set :protection, except: :session_hijacking
set :bind, '0.0.0.0'

Slim::Engine.set_default_options pretty: true


##############   DB  #############################
DataMapper::setup(:default, "sqlite3://#{Dir.pwd}/data.db")

class Point
  include DataMapper::Resource
  property :id,   Serial

  property :time, DateTime
  property :lat, Float
  property :long, Float
  property :accuracy, Float
  property :device, String

end

class User
  include DataMapper::Resource
  property :id,   Serial

  property :name, String
  property :password, String
end

DataMapper.finalize
DataMapper.auto_upgrade!
#DataMapper::Model.raise_on_save_failure = true

##############  ~ DB  #############################

helpers do

  def protected!
    return true
    unless authorized?
      response['WWW-Authenticate'] = %(Basic realm="Restricted Area")
      throw(:halt, [401, "Not authorized\n"])
    end
  end

  def authorized?
    @auth ||=  Rack::Auth::Basic::Request.new(request.env)
    if @auth.provided? && @auth.basic? && @auth.credentials
      logger.info @auth.credentials.pretty_inspect
      return true if @auth.credentials == ['admin', 'admin']
      user=User.first(:name => @auth.credentials[0])
      return false if user.nil?
      user.password == @auth.credentials[1]
    end
  end

end

before do
  #pass if request.path_info == '/unaothorized'
  protected!

end

##########################  PATHS #############################

get '/' do # %r{/(.*)?} do |command|
  start=Time.at(0)
  fin=Time.now
  res=nil
  if ! params['start'].nil?
    start=Time.at(params['start'].to_i)
  end
  if ! params['end'].nil?
    fin=Time.at(params['end'].to_i)
  end
  if ! params['limit'].nil?
    lim=params['limit'].to_i
    res=Point.all(:time.gt =>start, :time.lt =>fin, :limit => lim).to_a
  else
    res=Point.all(:time.gt =>start, :time.lt =>fin).to_a
  end

  #logger.info "Start=#{start} end=#{fin} result=#{res}"
  res.map{|y| x={};x[:lat]=y.lat;x[:long]=y.long;x[:accuracy]=y.accuracy;x[:device]=y.device;x[:time]=y.time.to_time.to_i;x}.to_json
end

post '/' do
  data=Point.create
  f=params['lat'].to_f
  data.lat=params['lat'].to_f
  data.long=params['long'].to_f
  data.time=Time.at(params['time'].to_i)
  data.accuracy=params['accuracy'].to_f
  data.device=params['device']

  #logger.info "NEW: (#{f}) #{data.to_s}"
  data.save
  ''
end

